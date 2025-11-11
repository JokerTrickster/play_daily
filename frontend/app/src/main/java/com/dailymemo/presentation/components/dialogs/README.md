# RoomPasswordResetDialog Component

Material3 dialog component for room password reset with two-step UX flow.

## Features

- **Two-Step Flow**: Confirmation → Result Display
- **Material3 Design**: Follows Material Design 3 guidelines
- **Clipboard Integration**: One-tap copy to clipboard with visual feedback
- **Loading States**: Shows loading indicator during API calls
- **Accessible**: Proper content descriptions and screen reader support
- **Responsive**: Adapts to different screen sizes

## Component Structure

### Step 1: Confirmation Dialog
- Warning icon (Material Icons Warning)
- Clear explanation of consequences
- Highlighted warning section with consequences list
- Cancel/Confirm buttons
- Loading state support

### Step 2: Result Display
- Success message
- Large, readable password display (with letter spacing)
- Copy to clipboard button
- Visual feedback after copying (auto-hides after 2s)
- Reminder to share with participants

## Usage

```kotlin
import com.dailymemo.presentation.components.dialogs.RoomPasswordResetDialog

@Composable
fun YourScreen() {
    var showResetDialog by remember { mutableStateOf(false) }
    var newPassword by remember { mutableStateOf<String?>(null) }
    var isResetting by remember { mutableStateOf(false) }

    // ... your screen content

    RoomPasswordResetDialog(
        showDialog = showResetDialog,
        onDismiss = {
            showResetDialog = false
            newPassword = null
        },
        onConfirmReset = {
            isResetting = true
            // API call integration (task #63)
            viewModel.resetRoomPassword(roomId)
        },
        newPassword = newPassword, // Set this when API returns success
        isLoading = isResetting
    )
}
```

## Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `showDialog` | Boolean | Controls dialog visibility |
| `onDismiss` | () -> Unit | Callback when dialog is dismissed |
| `onConfirmReset` | () -> Unit | Callback when user confirms reset |
| `newPassword` | String? | New password to display (null = confirmation step) |
| `isLoading` | Boolean | Shows loading indicator during API call |

## State Management

The dialog automatically switches between confirmation and result steps based on the `newPassword` parameter:

- `newPassword == null` → Confirmation step
- `newPassword != null` → Result step

## Integration with ViewModel (Task #63)

```kotlin
// ViewModel
sealed class PasswordResetState {
    object Idle : PasswordResetState()
    object Loading : PasswordResetState()
    data class Success(val newPassword: String) : PasswordResetState()
    data class Error(val message: String) : PasswordResetState()
}

class RoomViewModel : ViewModel() {
    private val _resetState = MutableStateFlow<PasswordResetState>(PasswordResetState.Idle)
    val resetState: StateFlow<PasswordResetState> = _resetState.asStateFlow()

    fun resetRoomPassword(roomId: Long) {
        viewModelScope.launch {
            _resetState.value = PasswordResetState.Loading
            try {
                val response = repository.resetRoomPassword(roomId)
                _resetState.value = PasswordResetState.Success(response.newPassword)
            } catch (e: Exception) {
                _resetState.value = PasswordResetState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun clearResetState() {
        _resetState.value = PasswordResetState.Idle
    }
}

// Screen
val resetState by viewModel.resetState.collectAsState()

var showResetDialog by remember { mutableStateOf(false) }

RoomPasswordResetDialog(
    showDialog = showResetDialog,
    onDismiss = {
        showResetDialog = false
        viewModel.clearResetState()
    },
    onConfirmReset = {
        viewModel.resetRoomPassword(roomId)
    },
    newPassword = (resetState as? PasswordResetState.Success)?.newPassword,
    isLoading = resetState is PasswordResetState.Loading
)
```

## Design Decisions

### Two-Step Flow
- **Why**: Prevents accidental password resets and ensures users understand consequences
- **User Flow**: Read warning → Confirm → Copy password → Share with team

### Visual Hierarchy
- **Confirmation Step**: Error colors (red) for warning emphasis
- **Result Step**: Primary colors (blue) for success state
- **Password Display**: Large text with letter spacing for easy reading

### Clipboard UX
- **Auto-feedback**: Shows "copied" message for 2 seconds
- **No toast required**: In-dialog feedback is cleaner and more contextual
- **One-tap copy**: Reduces friction in workflow

### Accessibility
- Content descriptions on all icons
- High contrast colors
- Large touch targets (48dp minimum)
- Screen reader friendly text
- Keyboard navigation support

## Localization

All strings are externalized to `strings.xml`:

### English Translations Needed

If adding English support, add these to `values/strings.xml`:

```xml
<!-- Room Password Reset -->
<string name="password_reset_confirm_title">Reset Password</string>
<string name="password_reset_success_title">New Password</string>
<string name="password_reset_warning_message">Do you want to reset the room password?</string>
<string name="password_reset_consequences_title">Important</string>
<string name="password_reset_consequence_1">A new 4-digit password will be generated</string>
<string name="password_reset_consequence_2">You must share the new password with all participants</string>
<string name="password_reset_proceed_question">Continue?</string>
<string name="password_reset_button">Reset Password</string>
<string name="password_reset_new_password_message">A new room password has been generated</string>
<string name="password_reset_new_password_label">New Password</string>
<string name="password_reset_copy_button">Copy to Clipboard</string>
<string name="password_reset_copied_message">Password copied</string>
<string name="password_reset_share_reminder">Share the new password with participants</string>
```

## Testing

See `RoomPasswordResetDialogPreview.kt` for:
- Confirmation step preview
- Loading state preview
- Result step preview
- Interactive preview for testing

## Future Enhancements

Potential improvements for future iterations:
- Share button (Android share sheet)
- QR code generation for password
- Undo functionality (time-limited)
- Password strength indicator
- Custom password option
- History of password changes
