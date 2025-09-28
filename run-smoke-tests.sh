#!/bin/bash

# E2E Smoke Tests Runner Script
# This script runs only the smoke tests defined in the TestNG suite XML

set -e

echo "🚀 Starting E2E Smoke Tests..."
echo "================================="

# Set default values
BROWSER=${browser:-chrome}
HEADLESS=${headless:-false}
ENVIRONMENT=${environment:-local}

echo "Configuration:"
echo "- Browser: $BROWSER"
echo "- Headless: $HEADLESS"
echo "- Environment: $ENVIRONMENT"
echo ""

# Build classpath
echo "📚 Building classpath..."
CLASSPATH=$(mvn -q dependency:build-classpath -Dmdep.outputFile=/dev/stdout):target/test-classes:target/classes

# Run TestNG directly
echo "🧪 Running TestNG smoke tests..."
java -cp "$CLASSPATH" \
    -Dbrowser="$BROWSER" \
    -Dheadless="$HEADLESS" \
    -Denvironment="$ENVIRONMENT" \
    org.testng.TestNG \
    -d target/testng-reports \
    src/test/resources/testng/smoke-testng.xml

echo ""
echo "✅ Smoke tests completed!"
echo "📊 Reports available in:"
echo "   - target/testng-reports/"
echo "   - target/extent-reports/"
echo "   - target/screenshots/"