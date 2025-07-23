# 📸 Receiptr App Screenshots

This directory contains all the UI screenshots and images used in the main README and UI_SHOWCASE documentation.

## 📁 File Structure

### Authentication Flow
- `01_splash_screen.png` - App launch screen with logo
- `02_welcome_screen.png` - Onboarding and welcome interface
- `03_login_options.png` - Authentication method selection
- `04_email_auth.png` - Email/password login screen
- `05_phone_auth_step1.png` - Phone number input
- `06_phone_auth_step2.png` - OTP verification
- `07_registration.png` - New user registration

### Main Application
- `08_home_screen.png` - Dashboard with spending overview
- `09_bottom_navigation.png` - Navigation bar
- `10_receipts_list.png` - Receipt management screen
- `11_receipts_empty_state.png` - Empty receipts state
- `12_receipts_search.png` - Search and filter functionality
- `13_receipt_detail.png` - Individual receipt details

### Camera & Scanning
- `14_pdf_preview.png` - Generated PDF preview
- `15_pdf_sharing.png` - PDF sharing options
- `16_scan_camera.png` - Camera interface for receipt capture
- `17_photo_preview.png` - Captured image preview
- `18_ml_results.png` - ML Kit processing results
- `19_review_receipt.png` - Data review and editing

### Analytics & Reports
- `20_analytics_overview.png` - Analytics dashboard
- `21_pie_chart.png` - Category spending pie chart
- `22_spending_trends.png` - Spending trend analysis
- `23_weekly_report.png` - Weekly spending report
- `24_budget_management.png` - Budget tracking interface

### Settings & Profile
- `25_settings.png` - App settings screen
- `26_profile.png` - User profile management
- `27_notification_settings.png` - Notification preferences

### Email Integration
- `28_email_integration.png` - Email provider setup
- `29_email_receipts.png` - Imported email receipts
- `30_email_processing.png` - Email receipt processing

### Notifications
- `31_notification_success.png` - Success notification example
- `32_notification_budget.png` - Budget alert notification
- `33_notification_summary.png` - Weekly summary notification
- `34_notification_center.png` - In-app notification history

### Loading & Error States
- `35_skeleton_loading.png` - Skeleton loading animations
- `36_progress_indicators.png` - Progress indicators
- `37_error_network.png` - Network error handling
- `38_error_camera.png` - Camera permission errors
- `39_empty_states.png` - Various empty state designs

### Preview Images (for README table)
- `auth_preview.png` - Authentication flow preview
- `home_preview.png` - Home screen preview
- `scan_preview.png` - Scanning flow preview
- `receipts_preview.png` - Receipt management preview
- `analytics_preview.png` - Analytics dashboard preview
- `settings_preview.png` - Settings panel preview

## 📐 Image Guidelines

### Dimensions
- **Phone Screenshots**: 1080x1920 pixels (9:16 aspect ratio)
- **Tablet Screenshots**: 1200x1600 pixels (3:4 aspect ratio)
- **Preview Images**: 800x600 pixels (4:3 aspect ratio)

### Quality Standards
- **Format**: PNG for screenshots, JPG for photos
- **DPI**: 300 DPI for high-quality display
- **Device Frame**: Use consistent Android device frames
- **Content**: Show realistic data, avoid lorem ipsum text
- **Theme**: Include both light and dark mode examples

### Best Practices
1. **Consistent Branding**: Use app colors and fonts
2. **Clear UI Elements**: Ensure all text and buttons are readable
3. **Realistic Data**: Show actual receipt data and spending amounts
4. **Device Context**: Include status bar and navigation elements
5. **Accessibility**: Consider high contrast and larger text versions

## 🔧 How to Add Screenshots

### Using Android Studio
1. Run the app on an emulator or device
2. Navigate to desired screen
3. Use `Ctrl+Shift+S` (Windows) or `Cmd+Shift+S` (Mac)
4. Save to this directory with appropriate filename

### Using ADB Commands
```bash
# Take screenshot
adb shell screencap -p /sdcard/screenshot.png

# Pull to computer
adb pull /sdcard/screenshot.png screenshots/01_splash_screen.png
```

### Using Device Screenshot Tools
- **Android 4.0+**: Power + Volume Down
- **Samsung**: Power + Home button
- **Screenshot Apps**: Various third-party apps available

## 📱 Testing Different States

### Light/Dark Mode
```kotlin
// Toggle theme in settings or use system theme
AppTheme(darkTheme = true) {
    YourScreen()
}
```

### Different Screen Sizes
- Test on phones (5", 6", 6.5"+)
- Test on tablets (7", 10", 12"+)
- Test on foldable devices

### Various Data States
- Empty states (no receipts, no data)
- Loading states (skeleton loaders)
- Error states (network issues, permissions)
- Success states (completed actions)

## 🎨 Image Editing Tools

### Free Options
- **GIMP**: Full-featured image editor
- **Canva**: Online design tool
- **Android Asset Studio**: Google's official tool

### Professional Options
- **Adobe Photoshop**: Industry standard
- **Sketch**: UI/UX focused (Mac only)
- **Figma**: Collaborative design tool

## 📋 Checklist Before Adding Screenshots

- [ ] Image is high quality and properly sized
- [ ] UI elements are clearly visible and readable
- [ ] Realistic data is displayed (no placeholder text)
- [ ] Device frame is consistent with other screenshots
- [ ] File naming follows the convention
- [ ] Both light and dark mode versions (if applicable)
- [ ] Image is optimized for web display

---

**Note**: Screenshots should be updated regularly to reflect the latest app design and features. Always use the most recent version of the app when capturing screenshots.

**Last Updated**: January 2025
