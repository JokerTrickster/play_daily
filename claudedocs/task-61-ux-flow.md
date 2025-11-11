# RoomPasswordResetDialog - UX Flow & Visual Design

## Dialog Flow

```
User clicks "Reset Password" button
         ↓
┌─────────────────────────────────────────────┐
│  [⚠️]  비밀번호 재설정                       │ ← Warning icon
├─────────────────────────────────────────────┤
│                                             │
│  방 비밀번호를 재설정하시겠습니까?             │ ← Clear question
│                                             │
│  ┌───────────────────────────────────────┐ │
│  │ 주의사항                               │ │ ← Error container
│  │ • 새로운 4자리 비밀번호가 생성됩니다     │ │
│  │ • 모든 참여자에게 새 비밀번호를         │ │
│  │   공유해야 합니다                       │ │
│  └───────────────────────────────────────┘ │
│                                             │
│  계속하시겠습니까?                           │ ← Confirmation
│                                             │
├─────────────────────────────────────────────┤
│           [취소]    [비밀번호 재설정]        │ ← Actions
└─────────────────────────────────────────────┘
         ↓ User clicks "비밀번호 재설정"
         ↓
┌─────────────────────────────────────────────┐
│  [⚠️]  비밀번호 재설정                       │
├─────────────────────────────────────────────┤
│  ... (same content as above)                │
├─────────────────────────────────────────────┤
│           [취소]    [⟳]                     │ ← Loading state
└─────────────────────────────────────────────┘
         ↓ API call completes
         ↓
┌─────────────────────────────────────────────┐
│         새 비밀번호                          │ ← Success title
├─────────────────────────────────────────────┤
│                                             │
│  새로운 방 비밀번호가 생성되었습니다          │ ← Success message
│                                             │
│  ┌───────────────────────────────────────┐ │
│  │         새 비밀번호                    │ │ ← Primary container
│  │                                       │ │
│  │            1 2 3 4                    │ │ ← Large, spaced
│  │                                       │ │
│  └───────────────────────────────────────┘ │
│                                             │
│  [📋 클립보드에 복사]                        │ ← Copy button
│                                             │
│  ┌───────────────────────────────────────┐ │
│  │   비밀번호가 복사되었습니다            │ │ ← Feedback (2s)
│  └───────────────────────────────────────┘ │
│                                             │
│  참여자들에게 새 비밀번호를 공유하세요        │ ← Reminder
│                                             │
├─────────────────────────────────────────────┤
│                  [닫기]                     │ ← Close action
└─────────────────────────────────────────────┘
```

## Visual Design Specifications

### Step 1: Confirmation Dialog

**Dimensions**
- Width: fillMaxWidth() - 32.dp padding
- Height: Auto (wrap content)
- Corner radius: 16.dp
- Elevation: 8.dp (tonal)

**Colors**
- Background: surface
- Warning icon: error (red)
- Title text: onSurface
- Body text: onSurface
- Consequence box: errorContainer (alpha 0.3)
- Consequence text: onSurface
- Consequence title: error
- Cancel button: default TextButton
- Confirm button: error background

**Typography**
- Title: titleLarge, FontWeight.Bold
- Body: bodyMedium
- Consequences title: labelLarge, FontWeight.Bold
- Consequence items: bodySmall
- Buttons: default button text

**Spacing**
- Icon size: 32.dp
- Section spacing: 12.dp
- Consequence box padding: 12.dp
- Consequence item spacing: 4.dp

### Step 2: Result Dialog

**Dimensions**
- Width: fillMaxWidth() - 32.dp padding
- Height: Auto (wrap content)
- Corner radius: 16.dp
- Password container: 12.dp corner radius
- Elevation: 8.dp (tonal)

**Colors**
- Background: surface
- Title text: onSurface
- Message text: onSurface
- Password container: primaryContainer
- Password label: onPrimaryContainer
- Password text: primary
- Copy button: outline with default colors
- Feedback box: tertiaryContainer
- Feedback text: onTertiaryContainer
- Reminder text: onSurfaceVariant

**Typography**
- Title: titleLarge, FontWeight.Bold
- Message: bodyMedium
- Password label: labelMedium
- Password: displaySmall, FontWeight.Bold, letterSpacing 4.sp
- Copy button: default button text
- Feedback: labelMedium
- Reminder: bodySmall

**Spacing**
- Overall section spacing: 16.dp
- Password container padding: 20.dp
- Password label spacing: 8.dp
- Copy button spacing: 8.dp
- Copy button icon spacing: 8.dp
- Feedback padding: 16.dp (h) × 8.dp (v)

## Interaction States

### Confirmation Step

| State | Visual Feedback |
|-------|----------------|
| Default | Both buttons enabled, normal colors |
| Loading | Cancel disabled, Confirm shows spinner, text hidden |
| Disabled | Cannot dismiss dialog during loading |

### Result Step

| State | Visual Feedback |
|-------|----------------|
| Initial | Password displayed, copy button ready |
| Copying | Immediate clipboard copy, feedback appears |
| Copied | Green feedback box visible for 2 seconds |
| After feedback | Feedback fades out, copy button ready again |

## Accessibility

### Screen Reader Flow

**Confirmation Step**
1. "경고, 비밀번호 재설정"
2. "방 비밀번호를 재설정하시겠습니까?"
3. "주의사항"
4. "새로운 4자리 비밀번호가 생성됩니다"
5. "모든 참여자에게 새 비밀번호를 공유해야 합니다"
6. "계속하시겠습니까?"
7. "취소, 버튼"
8. "비밀번호 재설정, 버튼"

**Result Step**
1. "새 비밀번호"
2. "새로운 방 비밀번호가 생성되었습니다"
3. "새 비밀번호"
4. "1234" (or actual password)
5. "클립보드에 복사, 버튼"
6. (after copy) "비밀번호가 복사되었습니다"
7. "참여자들에게 새 비밀번호를 공유하세요"
8. "닫기, 버튼"

### Touch Targets

All interactive elements meet minimum 48dp × 48dp touch target:
- Cancel button: ≥ 48dp height
- Confirm button: ≥ 48dp height
- Copy button: ≥ 48dp height
- Close button: ≥ 48dp height
- Icon button: Already 48dp × 48dp

### Keyboard Navigation

Material3 AlertDialog provides:
- Tab navigation between focusable elements
- Enter/Space to activate buttons
- Escape to dismiss (when not loading)
- Focus indicator on all buttons

## Animation & Timing

### Transitions
- Dialog appear: Material3 default fade + scale (300ms)
- Dialog dismiss: Material3 default fade + scale (200ms)
- Step transition: None (instant, component swap)

### Loading State
- Spinner rotation: Continuous smooth rotation
- Button text fade: Instant hide when loading

### Feedback
- Feedback appear: Instant
- Feedback linger: 2000ms
- Feedback disappear: Instant

## Clipboard Behavior

### Android Clipboard API
```kotlin
val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
val clip = ClipData.newPlainText("Room Password", text)
clipboardManager.setPrimaryClip(clip)
```

### User Experience
1. User taps "클립보드에 복사"
2. Clipboard immediately receives password
3. Feedback appears: "비밀번호가 복사되었습니다"
4. After 2 seconds, feedback auto-hides
5. User can copy again if needed

### Privacy Considerations
- Clipboard content is accessible by other apps
- Password is plain text in clipboard
- Consider showing brief security reminder (future enhancement)

## Responsive Design

### Phone (Default)
- Dialog width: Screen width - 32.dp
- Single column layout
- Buttons fill available width

### Tablet (Material3 defaults)
- Dialog width: Max 560.dp
- Same single column layout
- Centered on screen

### Landscape
- Dialog scrolls if needed
- All content remains visible
- Same layout structure

## Error Handling

### API Errors (Not implemented in Task #61)

Future implementation in Task #63:
- Show error message in dialog
- Allow retry
- Don't dismiss on error
- Clear error on retry

### Clipboard Errors

Clipboard operations are fail-safe:
- Silent failure if clipboard unavailable
- Feedback still shows (user can manually copy)
- No crash or exception

## Component State Management

```kotlin
// Parent component state
var showResetDialog by remember { mutableStateOf(false) }
var newPassword by remember { mutableStateOf<String?>(null) }
var isResetting by remember { mutableStateOf(false) }

// Dialog determines step based on newPassword
val isResultStep = newPassword != null

// Step 1: newPassword = null, isLoading = false
// Step 1 (loading): newPassword = null, isLoading = true
// Step 2: newPassword = "1234", isLoading = false
```

## Testing Scenarios

### Manual Testing Checklist

**Confirmation Step**
- [ ] Dialog appears centered on screen
- [ ] Warning icon is red and visible
- [ ] Title is bold and prominent
- [ ] Warning message is clear
- [ ] Consequence box is highlighted
- [ ] Both consequences are listed
- [ ] Question text is visible
- [ ] Cancel button dismisses dialog
- [ ] Confirm button shows loading spinner
- [ ] Cannot dismiss during loading

**Result Step**
- [ ] Title changes to success message
- [ ] New password is displayed large and clear
- [ ] Password has proper letter spacing
- [ ] Copy button is visible
- [ ] Tapping copy shows feedback
- [ ] Feedback disappears after 2s
- [ ] Can copy multiple times
- [ ] Reminder message is visible
- [ ] Close button dismisses dialog

**Accessibility**
- [ ] Screen reader announces all content
- [ ] Tab navigation works correctly
- [ ] All buttons are focusable
- [ ] High contrast is sufficient
- [ ] Touch targets are adequate

**Edge Cases**
- [ ] Very long password (unlikely with 4 digits)
- [ ] Rapid button tapping
- [ ] Dialog rotation handling
- [ ] Background app during loading
- [ ] System clipboard disabled

## Usage Example (Complete Flow)

```kotlin
@Composable
fun ParticipantManagementScreen(
    roomId: Long,
    viewModel: RoomViewModel
) {
    val resetState by viewModel.resetState.collectAsState()

    var showResetDialog by remember { mutableStateOf(false) }

    // Reset button in toolbar or menu
    IconButton(onClick = { showResetDialog = true }) {
        Icon(Icons.Default.LockReset, "Reset Password")
    }

    // Dialog
    RoomPasswordResetDialog(
        showDialog = showResetDialog,
        onDismiss = {
            showResetDialog = false
            viewModel.clearResetState()
        },
        onConfirmReset = {
            viewModel.resetRoomPassword(roomId)
        },
        newPassword = when (resetState) {
            is PasswordResetState.Success -> resetState.newPassword
            else -> null
        },
        isLoading = resetState is PasswordResetState.Loading
    )

    // Error handling (separate Snackbar)
    if (resetState is PasswordResetState.Error) {
        LaunchedEffect(resetState) {
            // Show error Snackbar
            snackbarHostState.showSnackbar(resetState.message)
            viewModel.clearResetState()
        }
    }
}
```

## Metrics & Analytics (Future)

Potential tracking points:
- Dialog opened
- Dialog dismissed (without reset)
- Reset confirmed
- Password copied
- Time to copy after display
- Multiple copy attempts
- Dialog closed after success
