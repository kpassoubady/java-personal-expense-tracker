pipeline {
    agent any
    
    parameters {
        choice(
            choices: ['chrome', 'firefox'],
            description: 'Browser for test execution',
            name: 'BROWSER'
        )
        booleanParam(
            defaultValue: true,
            description: 'Run tests in headless mode',
            name: 'HEADLESS'
        )
        choice(
            choices: ['staging', 'production'],
            description: 'Test environment',
            name: 'ENVIRONMENT'
        )
        string(
            defaultValue: '2',
            description: 'Number of parallel threads',
            name: 'PARALLEL_COUNT'
        )
    }
    
    environment {
        JAVA_HOME = '/usr/lib/jvm/java-21-openjdk'
        MAVEN_HOME = '/usr/local/maven'
        PATH = "${MAVEN_HOME}/bin:${JAVA_HOME}/bin:${PATH}"
    }
    
    triggers {
        // Run tests daily at 2 AM
        cron('0 2 * * *')
        // Trigger on SCM changes
        pollSCM('H/15 * * * *')
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    env.GIT_COMMIT = sh(returnStdout: true, script: 'git rev-parse HEAD').trim()
                    env.GIT_BRANCH = env.BRANCH_NAME
                }
            }
        }
        
        stage('Setup Environment') {
            steps {
                sh 'java -version'
                sh 'mvn -version'
                
                // Install browser dependencies
                sh '''
                    if ! command -v google-chrome &> /dev/null; then
                        wget -q -O - https://dl.google.com/linux/linux_signing_key.pub | sudo apt-key add -
                        sudo sh -c 'echo "deb [arch=amd64] http://dl.google.com/linux/chrome/deb/ stable main" >> /etc/apt/sources.list.d/google-chrome.list'
                        sudo apt-get update
                        sudo apt-get install -y google-chrome-stable
                    fi
                    
                    if ! command -v firefox &> /dev/null; then
                        sudo apt-get install -y firefox
                    fi
                '''
            }
        }
        
        stage('Build Application') {
            steps {
                sh 'mvn clean compile'
            }
        }
        
        stage('Start Application') {
            steps {
                script {
                    // Start Spring Boot application in background
                    sh '''
                        nohup mvn spring-boot:run > app.log 2>&1 &
                        echo $! > app.pid
                        
                        # Wait for application to start
                        timeout 60 bash -c 'until curl -s http://localhost:8080 > /dev/null; do sleep 2; done'
                    '''
                }
            }
        }
        
        stage('Run E2E Tests') {
            steps {
                script {
                    try {
                        sh """
                            mvn test \\
                                -Dbrowser=${params.BROWSER} \\
                                -Dheadless=${params.HEADLESS} \\
                                -Denvironment=${params.ENVIRONMENT} \\
                                -DparallelCount=${params.PARALLEL_COUNT} \\
                                -DBUILD_NUMBER=${BUILD_NUMBER} \\
                                -DBUILD_URL=${BUILD_URL} \\
                                -DGIT_BRANCH=${env.GIT_BRANCH} \\
                                -DGIT_COMMIT=${env.GIT_COMMIT}
                        """
                    } catch (Exception e) {
                        currentBuild.result = 'UNSTABLE'
                        echo "Tests failed: ${e.getMessage()}"
                    }
                }
            }
            post {
                always {
                    // Collect test results
                    publishTestResults testResultsPattern: 'target/surefire-reports/*.xml'
                    
                    // Archive test reports
                    archiveArtifacts artifacts: '''
                        target/surefire-reports/**/*,
                        target/site/jacoco/**/*,
                        test-output/**/*,
                        target/allure-reports/**/*
                    ''', fingerprint: true, allowEmptyArchive: true
                    
                    // Archive screenshots on failure
                    script {
                        if (currentBuild.result == 'UNSTABLE' || currentBuild.result == 'FAILURE') {
                            archiveArtifacts artifacts: 'test-output/screenshots/**/*', allowEmptyArchive: true
                        }
                    }
                }
            }
        }
        
        stage('Generate Reports') {
            parallel {
                stage('Allure Report') {
                    steps {
                        script {
                            try {
                                sh 'mvn allure:report'
                                publishHTML([
                                    allowMissing: false,
                                    alwaysLinkToLastBuild: true,
                                    keepAll: true,
                                    reportDir: 'target/allure-reports',
                                    reportFiles: 'index.html',
                                    reportName: 'Allure Test Report'
                                ])
                            } catch (Exception e) {
                                echo "Failed to generate Allure report: ${e.getMessage()}"
                            }
                        }
                    }
                }
                
                stage('ExtentReports') {
                    steps {
                        publishHTML([
                            allowMissing: false,
                            alwaysLinkToLastBuild: true,
                            keepAll: true,
                            reportDir: 'test-output/reports',
                            reportFiles: '*.html',
                            reportName: 'ExtentReports'
                        ])
                    }
                }
                
                stage('JaCoCo Coverage') {
                    steps {
                        publishHTML([
                            allowMissing: false,
                            alwaysLinkToLastBuild: true,
                            keepAll: true,
                            reportDir: 'target/site/jacoco',
                            reportFiles: 'index.html',
                            reportName: 'JaCoCo Coverage Report'
                        ])
                        
                        // Publish coverage results
                        publishCoverage adapters: [jacocoAdapter('target/site/jacoco/jacoco.xml')], sourceFileResolver: sourceFiles('STORE_LAST_BUILD')
                    }
                }
            }
        }
        
        stage('Test Metrics') {
            steps {
                script {
                    // Custom test metrics collection
                    sh '''
                        # Extract test metrics from surefire reports
                        if [ -d "target/surefire-reports" ]; then
                            TESTS=$(find target/surefire-reports -name "*.xml" -exec grep -h "tests=" {} \\; | head -1 | sed 's/.*tests="\\([^"]*\\)".*/\\1/')
                            FAILURES=$(find target/surefire-reports -name "*.xml" -exec grep -h "failures=" {} \\; | head -1 | sed 's/.*failures="\\([^"]*\\)".*/\\1/')
                            ERRORS=$(find target/surefire-reports -name "*.xml" -exec grep -h "errors=" {} \\; | head -1 | sed 's/.*errors="\\([^"]*\\)".*/\\1/')
                            
                            echo "TOTAL_TESTS=${TESTS:-0}" > test_metrics.properties
                            echo "FAILED_TESTS=${FAILURES:-0}" >> test_metrics.properties
                            echo "ERROR_TESTS=${ERRORS:-0}" >> test_metrics.properties
                            echo "SUCCESS_RATE=$(echo "scale=2; (${TESTS:-0} - ${FAILURES:-0} - ${ERRORS:-0}) * 100 / ${TESTS:-1}" | bc)" >> test_metrics.properties
                        fi
                    '''
                    
                    // Archive metrics
                    archiveArtifacts artifacts: 'test_metrics.properties', allowEmptyArchive: true
                }
            }
        }
    }
    
    post {
        always {
            script {
                // Stop application
                sh '''
                    if [ -f app.pid ]; then
                        kill $(cat app.pid) || true
                        rm -f app.pid
                    fi
                '''
                
                // Clean up old reports (keep last 30 builds)
                sh 'find test-output -name "*.html" -mtime +30 -delete || true'
                sh 'find test-output/screenshots -name "*.png" -mtime +7 -delete || true'
            }
        }
        
        failure {
            script {
                // Send email notification on failure
                emailext (
                    subject: "[Expense Tracker] E2E Tests Failed - Build #${BUILD_NUMBER}",
                    body: """
                        E2E Tests failed for branch: ${env.GIT_BRANCH}
                        Build Number: ${BUILD_NUMBER}
                        Browser: ${params.BROWSER}
                        Environment: ${params.ENVIRONMENT}
                        
                        Build URL: ${BUILD_URL}
                        
                        Please check the test reports and screenshots for more details.
                    """,
                    to: "${env.NOTIFICATION_EMAIL}",
                    attachmentsPattern: "test_metrics.properties"
                )
                
                // Send Slack notification
                slackSend(
                    channel: '#qa-alerts',
                    color: 'danger',
                    message: """
                        :x: E2E Tests Failed
                        Repository: ${env.JOB_NAME}
                        Branch: ${env.GIT_BRANCH}
                        Build: #${BUILD_NUMBER}
                        Browser: ${params.BROWSER}
                        <${BUILD_URL}|View Build Details>
                    """
                )
            }
        }
        
        success {
            script {
                // Send success notification for scheduled builds
                if (currentBuild.getBuildCauses('hudson.triggers.TimerTrigger$TimerTriggerCause')) {
                    slackSend(
                        channel: '#qa-reports',
                        color: 'good',
                        message: """
                            :white_check_mark: Scheduled E2E Tests Passed
                            Repository: ${env.JOB_NAME}
                            Branch: ${env.GIT_BRANCH}
                            Build: #${BUILD_NUMBER}
                            Browser: ${params.BROWSER}
                        """
                    )
                }
            }
        }
        
        unstable {
            script {
                slackSend(
                    channel: '#qa-alerts',
                    color: 'warning',
                    message: """
                        :warning: E2E Tests Unstable
                        Repository: ${env.JOB_NAME}
                        Branch: ${env.GIT_BRANCH}
                        Build: #${BUILD_NUMBER}
                        Browser: ${params.BROWSER}
                        <${BUILD_URL}|View Build Details>
                    """
                )
            }
        }
    }
}