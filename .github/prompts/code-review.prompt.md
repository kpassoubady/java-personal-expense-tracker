# .github/prompts/code-review.prompt.md

---
title: "Code Review Assistant"
description: "Comprehensive code review with security and performance focus"
author: "Development Team"
tags: ["review", "security", "performance"]
context:
  - "#selection"
  - "#file"
variables:
  - name: "focus_areas"
    type: "array"
    default: ["security", "performance", "maintainability"]
---

## Prompt Template

Review the provided code focusing on {{focus_areas}}. 

### Analysis Areas:
1. **Security**: Check for vulnerabilities and secure coding practices
2. **Performance**: Identify optimization opportunities
3. **Maintainability**: Assess code clarity and documentation

### Output Format:
- Issues found (with severity levels)
- Specific recommendations
- Code examples for improvements

Please provide actionable feedback with line-specific suggestions.