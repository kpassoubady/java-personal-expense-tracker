# Template Backup Directory

This directory contains template files that were moved from the main application to reduce confusion and maintain a clean template structure.

## Files Moved - September 27, 2025

### From `/src/main/resources/templates/`:
- `home.html` - Unused standalone home template that was not being referenced by the HomeController

### From `/src/main/resources/templates/home/`:
- `dashboard-classic.html` - Classic dashboard template (used when `app.template.use-new-layout=false`)
- `dashboard-new.html` - Alternative new dashboard template
- `dashboard-with-layout.html` - Dashboard with layout integration variant
- `demo-layout-simple.html` - Simple layout demo template

## Currently Active Template

The application is currently using:
- **Active Template**: `/src/main/resources/templates/home/dashboard.html`
- **Configuration**: `app.template.use-new-layout=true` in `application.properties`
- **Controller Logic**: `HomeController` returns `"home/dashboard" + templateConfig.getTemplateSuffix()`
  - When `use-new-layout=true`: suffix is `""` → `home/dashboard.html`
  - When `use-new-layout=false`: suffix is `"-classic"` → `home/dashboard-classic.html`

## How to Restore

If you need to switch back to the classic layout or use any of these templates:

1. **To use classic layout**: 
   - Set `app.template.use-new-layout=false` in `application.properties`
   - Move `dashboard-classic.html` back to `/src/main/resources/templates/home/`

2. **To restore any specific template**:
   ```bash
   mv backup/templates/home/[template-name] src/main/resources/templates/home/
   ```

## Issues Fixed

Moving these files resolved:
- ✅ Template confusion and potential conflicts
- ✅ Horizontal page duplication issue (was caused by duplicate chart sections in templates)
- ✅ Chart visibility issues (resolved by fixing Jackson infinite recursion in Category entity)