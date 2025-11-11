# Room Discovery & Management User Guide

**Last Updated:** November 11, 2024
**Version:** 1.0

---

## Table of Contents

1. [Introduction](#introduction)
2. [Discovering Rooms](#discovering-rooms)
3. [Filtering and Sorting](#filtering-and-sorting)
4. [Resetting Room Password](#resetting-room-password)
5. [Troubleshooting](#troubleshooting)
6. [FAQ](#faq)

---

## Introduction

The Room Discovery feature allows you to find and join public rooms created by other users. You can browse rooms sorted by popularity (most liked) or by recency (newest first), making it easy to discover communities that match your interests.

### Key Features

- **Browse Public Rooms:** Discover rooms without needing an invite
- **Popular Rooms:** Find the most active and liked rooms
- **Recent Rooms:** Explore newly created communities
- **Password Reset:** Room owners can generate new secure passwords
- **No Login Required:** Browse rooms without authentication (joining requires login)

---

## Discovering Rooms

### Accessing Room Discovery

1. **Open the App:** Launch the Daily Memo application
2. **Navigate to Discovery:** Tap the "Discover Rooms" tab or menu item
3. **Browse Options:** You'll see two tabs:
   - **Popular:** Rooms with the most likes
   - **Recent:** Newest rooms

### Room Discovery Screen Layout

```
┌─────────────────────────────────────┐
│   🔍 Room Discovery                 │
├─────────────────────────────────────┤
│  [Popular]  [Recent]                │
├─────────────────────────────────────┤
│                                     │
│  📱 Tech Discussion                 │
│  Code: TECH2024  •  ❤️ 150 likes   │
│  Created 2 weeks ago                │
│                                     │
│  📚 Book Club                       │
│  Code: BOOK2024  •  ❤️ 89 likes    │
│  Created 1 month ago                │
│                                     │
│  🎨 Art & Design                    │
│  Code: ART2024   •  ❤️ 45 likes    │
│  Created 3 days ago                 │
│                                     │
│  [Load More]                        │
└─────────────────────────────────────┘
```

---

### Popular Rooms Tab

**What You See:**
- Rooms sorted by number of likes (most popular first)
- For rooms with equal likes, newer rooms appear first
- Shows room name, join code, like count, and creation date

**How to Use:**
1. Tap the **Popular** tab
2. Scroll through the list of rooms
3. Tap on a room to see details and join

**Best For:**
- Finding active, well-established communities
- Discovering trending rooms
- Joining rooms with engaged members

**Example:**
```
Tech Discussion (TECH2024)
❤️ 150 likes • Created Nov 1, 2024
→ Most popular room
```

---

### Recent Rooms Tab

**What You See:**
- Rooms sorted by creation date (newest first)
- For rooms created at the same time, more popular ones appear first
- Shows room name, join code, like count, and creation date

**How to Use:**
1. Tap the **Recent** tab
2. Browse newly created rooms
3. Tap on a room to see details and join

**Best For:**
- Finding new communities
- Discovering fresh content
- Being an early member of a room

**Example:**
```
New Study Group (STUDY2024)
❤️ 5 likes • Created Nov 11, 2024
→ Brand new room
```

---

### Joining a Room

**Steps:**
1. **Browse Discovery:** Find a room you want to join
2. **Tap the Room:** Opens the room details screen
3. **Tap "Join Room":** Enters the room code automatically
4. **Enter Password:** Type the room password (ask room owner if needed)
5. **Tap "Confirm":** You're now a member!

**Note:** You must be logged in to join a room. Room discovery is available without login, but joining requires authentication.

---

## Filtering and Sorting

### Pagination

Rooms are displayed in pages of 20 by default. You can:
- **Load More:** Tap the "Load More" button at the bottom
- **Scroll Down:** Automatically loads more rooms (infinite scroll)
- **Jump to Top:** Pull down to refresh and return to page 1

### Room Information Displayed

Each room card shows:

| Field | Description | Example |
|-------|-------------|---------|
| **Room Name** | Display name of the room | "Tech Discussion" |
| **Room Code** | Unique join code | "TECH2024" |
| **Like Count** | Number of likes received | ❤️ 150 likes |
| **Created Date** | When the room was created | "Created 2 weeks ago" |

### What You DON'T See

For privacy and security:
- Room passwords (must be obtained from room owner)
- Member count (coming in future update)
- Owner name (only owner ID is tracked)
- Private rooms (never shown in discovery)

---

## Resetting Room Password

### Who Can Reset Passwords?

**Only room owners** can reset room passwords. If you're a member but not the owner, you cannot reset the password.

### How to Reset Your Room Password

**Step-by-Step:**

1. **Open Your Room:** Navigate to a room you own
2. **Tap Room Settings:** Usually in the top-right menu (⋮ or ⚙️)
3. **Select "Reset Password":** Tap the reset password option
4. **Confirm Action:** The app will show a confirmation dialog:

```
┌─────────────────────────────────────┐
│  ⚠️  Reset Room Password?           │
├─────────────────────────────────────┤
│                                     │
│  This will generate a new secure    │
│  password for your room.            │
│                                     │
│  Current members can still access   │
│  the room, but you'll need to       │
│  share the new password with        │
│  anyone joining in the future.      │
│                                     │
│  [Cancel]  [Reset Password]         │
└─────────────────────────────────────┘
```

5. **View New Password:** After confirmation, you'll see:

```
┌─────────────────────────────────────┐
│  ✅ Password Reset Successful       │
├─────────────────────────────────────┤
│                                     │
│  New Password:                      │
│  ┌─────────────────────────────┐  │
│  │  aB3!xY9@kL5                │  │
│  │  [Copy] [Share]             │  │
│  └─────────────────────────────┘  │
│                                     │
│  Important: Save this password!     │
│  It won't be shown again.           │
│                                     │
│  [Done]                             │
└─────────────────────────────────────┘
```

6. **Copy or Share:** Tap "Copy" to copy the password, or "Share" to send it via your preferred method
7. **Save It:** Store the password securely (password manager, notes app, etc.)

---

### Password Reset Rules

**Rate Limiting:**
- You can reset your room password **3 times in 24 hours**
- After 3 resets, you must wait 24 hours before resetting again
- This prevents abuse and ensures security

**If You Hit the Limit:**
```
┌─────────────────────────────────────┐
│  ⚠️  Rate Limit Reached             │
├─────────────────────────────────────┤
│                                     │
│  You've reset your room password    │
│  3 times in the last 24 hours.      │
│                                     │
│  Please wait before trying again.   │
│                                     │
│  Try again after:                   │
│  Nov 12, 2024 at 3:45 PM            │
│                                     │
│  [OK]                               │
└─────────────────────────────────────┘
```

**Password Characteristics:**
- **Length:** Exactly 12 characters
- **Composition:**
  - At least 1 uppercase letter (A-Z)
  - At least 1 lowercase letter (a-z)
  - At least 1 digit (0-9)
  - At least 1 special character (!@#$%^&*)
- **Security:** Generated using cryptographically secure randomness

**Example Passwords:**
- `aB3!xY9@kL5`
- `X7#mN2$pQ9!`
- `K5@vB8!zT3%`

---

### Sharing the New Password

**Best Practices:**

1. **Use Secure Channels:**
   - Direct message within the app
   - Encrypted messaging (Signal, WhatsApp)
   - Password managers with sharing features

2. **Avoid Insecure Methods:**
   - ❌ Public social media posts
   - ❌ Email (unless encrypted)
   - ❌ SMS text messages
   - ❌ Comments or public forums

3. **Share Selectively:**
   - Only share with people you want to invite
   - Don't post the password publicly
   - Update members personally if you reset

---

## Troubleshooting

### Room Discovery Issues

#### Problem: No Rooms Appearing

**Possible Causes:**
- No public rooms exist yet
- Network connection issue
- App needs to be refreshed

**Solutions:**
1. Pull down to refresh the room list
2. Check your internet connection
3. Switch between Popular and Recent tabs
4. Restart the app

---

#### Problem: Rooms Not Updating

**Possible Causes:**
- Cached data is being displayed
- New rooms haven't been created
- Refresh needed

**Solutions:**
1. Pull down to refresh
2. Switch tabs to force reload
3. Close and reopen the discovery screen

---

#### Problem: Can't Join a Room

**Possible Causes:**
- Not logged in
- Incorrect password
- Room no longer exists
- Network issue

**Solutions:**
1. Make sure you're logged in (check top-right corner)
2. Verify the password with the room owner
3. Try a different room to test connectivity
4. Contact the room owner to confirm room status

---

### Password Reset Issues

#### Problem: "Reset Password" Button Grayed Out

**Possible Causes:**
- You're not the room owner
- Rate limit reached (3 resets in 24 hours)

**Solutions:**
1. Verify you're the room owner (check room settings)
2. Wait 24 hours if you've hit the rate limit
3. Contact app support if you believe this is an error

---

#### Problem: Password Reset Failed

**Possible Causes:**
- Network connection issue
- Server error
- Room no longer exists

**Solutions:**
1. Check your internet connection
2. Try again in a few moments
3. Refresh the room details screen
4. Contact support if issue persists

**Error Messages:**

**"Room not found"**
- The room may have been deleted
- Refresh your room list

**"Only room owner can reset password"**
- You're not the owner of this room
- Contact the actual owner to reset the password

**"Rate limit exceeded"**
- You've reset the password 3 times in 24 hours
- Wait for the cooldown period (shown in the error message)

---

#### Problem: Lost the New Password

**What Happens:**
- The password is only shown once after reset
- It's not stored or emailed
- You cannot retrieve it later

**Solutions:**
1. **Reset Again:** If you haven't hit the rate limit (3/24h), reset the password again
2. **Check Clipboard:** If you copied it, paste into a note
3. **Ask Members:** If someone joined recently, they might have it
4. **Wait for Rate Limit:** If you've hit the limit, wait 24 hours and reset again

**Prevention:**
- Always copy the password immediately
- Save it in a password manager
- Share it with yourself (email, notes app)
- Take a screenshot (then delete after saving securely)

---

## FAQ

### General Questions

**Q: Do I need to create an account to browse rooms?**
A: No, you can browse public rooms in the discovery screen without logging in. However, you need to be logged in to join a room.

**Q: Can I see private rooms in discovery?**
A: No, only public rooms appear in the discovery feature. Private rooms can only be joined with an invite from the owner.

**Q: How often does the room list update?**
A: The list updates each time you open the discovery screen or pull down to refresh. Rooms are sorted in real-time based on current like counts and creation dates.

**Q: Why don't I see member counts?**
A: Member counts are not currently displayed in the discovery screen. This feature is planned for a future update.

---

### Password Reset Questions

**Q: What happens to existing members when I reset the password?**
A: Existing members remain in the room and can continue accessing it. Only new users trying to join will need the new password.

**Q: Can I choose my own password?**
A: No, passwords are automatically generated for security. The system creates strong, random 12-character passwords that meet security requirements.

**Q: Why can I only reset 3 times in 24 hours?**
A: This rate limit prevents abuse and ensures passwords aren't changed too frequently, which could confuse members and create security issues.

**Q: Can I see my current room password?**
A: No, for security reasons, room passwords are not displayed after creation. You can only reset to a new password, which is shown once.

**Q: What if I'm the room owner but can't reset the password?**
A: Make sure you're logged in with the account that created the room. If you still can't reset, contact support.

---

### Discovery Questions

**Q: How are "popular" rooms determined?**
A: Popular rooms are sorted by the number of likes they've received. Rooms with more likes appear first. If two rooms have the same number of likes, the newer room is shown first.

**Q: How are "recent" rooms determined?**
A: Recent rooms are sorted by creation date, with the newest rooms appearing first. If multiple rooms were created at the exact same time, the more popular one appears first.

**Q: Can I filter rooms by category or topic?**
A: Not yet. Category-based filtering is planned for a future update. Currently, you can browse all public rooms sorted by popularity or recency.

**Q: Can I search for specific rooms by name?**
A: Not yet. Room search functionality is planned for a future update. Currently, you can browse and scroll through available rooms.

**Q: How many rooms are shown per page?**
A: The app displays 20 rooms per page by default. Tap "Load More" or scroll down to see additional rooms.

---

### Joining Rooms Questions

**Q: What do I need to join a room?**
A: You need:
1. To be logged in
2. The room code (shown in discovery)
3. The room password (obtain from room owner)

**Q: Where do I get the room password?**
A: Room passwords are set by the room owner and are not shown publicly. You need to:
- Ask the room owner directly
- Check any invite message you received
- Look for the password in the room's community channels

**Q: Can I join a room without the password?**
A: No, all rooms require a password to join. This ensures only invited users can access the room's content.

---

### Security Questions

**Q: Is room discovery secure?**
A: Yes, room discovery is designed with security in mind:
- Only public room information is shown (no passwords)
- Private rooms are never exposed
- Password resets are rate-limited
- All passwords are cryptographically secure
- Audit logs track all password changes

**Q: Can someone else reset my room password?**
A: No, only the room owner (the account that created the room) can reset the password. Other members cannot perform this action.

**Q: What happens if someone tries to brute-force password resets?**
A: The system limits password resets to 3 per 24 hours per room. All reset attempts are logged with IP addresses for security monitoring.

**Q: Is my data safe when browsing rooms?**
A: Yes, browsing room discovery doesn't expose your personal information. Your browsing activity is private.

---

## Getting Help

### Support Channels

**In-App Support:**
1. Open app settings (⚙️)
2. Tap "Help & Support"
3. Choose your issue category
4. Submit a support request

**Email Support:**
- support@dailymemo.app

**Response Times:**
- General questions: 24-48 hours
- Account issues: 12-24 hours
- Security issues: Within 6 hours

---

### Reporting Issues

**How to Report:**
1. Describe the problem clearly
2. Include steps to reproduce
3. Attach screenshots if possible
4. Note the date and time of the issue

**What to Include:**
- Your account username (not password!)
- Room code (if applicable)
- Device and app version
- Error messages (take screenshots)

---

## Tips and Best Practices

### For Room Browsers

1. **Try Both Tabs:** Check both Popular and Recent to find different types of rooms
2. **Refresh Regularly:** Pull down to refresh and see new rooms
3. **Note Room Codes:** Write down codes of rooms you want to join
4. **Ask for Passwords:** Contact room owners through other channels to get passwords

### For Room Owners

1. **Choose Memorable Codes:** Use descriptive room codes that are easy to share
2. **Secure Your Passwords:** Store reset passwords in a password manager
3. **Share Responsibly:** Only share passwords with people you trust
4. **Monitor Resets:** Keep track of how many times you've reset (max 3/24h)
5. **Update Members:** Let existing members know when you reset the password

### Security Tips

1. **Never Share Passwords Publicly:** Use direct, private channels only
2. **Use Secure Storage:** Store passwords in encrypted password managers
3. **Reset if Compromised:** If you suspect the password leaked, reset immediately
4. **Monitor Your Room:** Check member activity regularly
5. **Report Suspicious Activity:** Contact support if you notice anything unusual

---

## Glossary

**Room:** A shared space where users can collaborate and communicate
**Room Code:** A unique identifier used to find and join a room (e.g., "TECH2024")
**Room Password:** A secure password required to join a room
**Public Room:** A room visible in the discovery feature
**Private Room:** A room not shown in discovery (invite-only)
**Room Owner:** The user who created the room and has admin privileges
**Like:** A way to show appreciation for a room (increases popularity)
**Discovery:** The feature that lets you browse and find public rooms
**Rate Limit:** A restriction on how many times you can perform an action in a time period
**Pagination:** Loading rooms in chunks (pages) instead of all at once

---

## Version History

### Version 1.0 (November 2024)
- Initial release of room discovery feature
- Popular rooms tab
- Recent rooms tab
- Password reset functionality
- Rate limiting (3 resets per 24 hours)
- Secure password generation

### Upcoming Features
- Room search by name
- Category-based filtering
- Member count display
- Room preview before joining
- Favorite rooms
- Recently viewed rooms

---

**Need more help?** Contact support@dailymemo.app or visit the in-app help center.
