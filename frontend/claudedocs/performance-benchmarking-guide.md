# Performance Benchmarking Guide

## Overview
Guide for measuring and validating performance targets for Issue #11.

---

## Performance Targets

| Metric | Target | Measurement Method |
|--------|--------|-------------------|
| App Startup (Cold) | < 2s | Android Studio Profiler / Benchmark |
| List Scroll FPS | ≥ 60fps | GPU Profiling / Visual Bars |
| Memory Usage (Stable) | No continuous growth | LeakCanary + Profiler |
| Image Compression | < 500KB | ImageCompressor metrics |
| Release APK Size | < 50MB | APK Analyzer |

---

## 1. App Startup Benchmark

### Method 1: Android Studio Profiler

```bash
# Enable cold start profiling
adb shell am force-stop com.dailymemo
adb shell am start -W -n com.dailymemo/.MainActivity

# Output shows:
# Starting: Intent { ... }
# Status: ok
# LaunchState: COLD
# Activity: com.dailymemo/.MainActivity
# TotalTime: <VALUE>    # This is your startup time
# WaitTime: <VALUE>
```

**Target**: TotalTime < 2000ms

### Method 2: Automated Benchmark

```bash
# Run startup benchmark test
./gradlew app:connectedAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=\
com.dailymemo.benchmark.StartupBenchmark
```

**Files**: `app/src/androidTest/java/com/dailymemo/benchmark/StartupBenchmark.kt`

---

## 2. Scroll Performance (60fps Target)

### GPU Profiling with Visual Bars

```bash
# Enable GPU profiling
adb shell setprop debug.hwui.profile visual_bars

# Restart app
adb shell am force-stop com.dailymemo
adb shell am start -n com.dailymemo/.MainActivity

# Navigate to list/timeline and scroll
# Green bars = <16ms (60fps) ✅
# Yellow bars = 16-32ms (30-60fps) ⚠️
# Red bars = >32ms (<30fps) ❌
```

**Target**: Majority green bars, no sustained red bars

### Systrace Analysis

```bash
# Capture systrace during scrolling
python $ANDROID_HOME/platform-tools/systrace/systrace.py \
  --time=10 -o trace.html sched gfx view wm am

# Open trace.html in Chrome
# Look for frame drops in "Frames" row
```

**Target**: <1% dropped frames during normal scrolling

---

## 3. Memory Leak Detection

### LeakCanary Setup

LeakCanary is automatically enabled in debug builds (added to build.gradle.kts).

**Usage**:
1. Install debug APK: `./gradlew installDebug`
2. Use app normally for 5-10 minutes
3. Create/delete 20+ memos
4. Navigate between screens repeatedly
5. LeakCanary automatically detects leaks and shows notification

**Expected**: Zero leaks detected

### Manual Memory Profiling

```bash
# Use Android Studio Profiler
# 1. Start app with profiler attached
# 2. Record memory allocation
# 3. Perform operations (create memos, navigate, search)
# 4. Force GC multiple times
# 5. Check for:
#    - Heap growth returns to baseline after GC
#    - No retained objects from destroyed Activities
#    - Bitmap objects properly recycled
```

**Target**:
- Heap returns to ±10MB of baseline after GC
- No Activity leaks
- <100MB total memory usage under normal load

---

## 4. Image Compression Metrics

### Measurement

Add logging to `ImageCompressor.kt`:

```kotlin
android.util.Log.d("ImageCompressor", """
    Original: ${originalSize / 1024}KB (${originalBitmap.width}×${originalBitmap.height})
    Scaled: ${scaledBitmap.width}×${scaledBitmap.height}
    Compressed: ${compressedBytes.size / 1024}KB at quality $quality
    Reduction: ${((1 - compressedBytes.size.toFloat() / originalSize) * 100).toInt()}%
""".trimIndent())
```

### Test Cases

| Original Size | Dimensions | Expected Output |
|--------------|------------|-----------------|
| 5MB | 4000×3000 | <500KB, 1920×1440 |
| 2MB | 2048×1536 | <500KB, 1365×1024 |
| 500KB | 1024×768 | <500KB, 1024×768 |

**Target**: All images <500KB, quality ≥70

---

## 5. Release Build Validation

### Build Release APK

```bash
# Clean and build release
./gradlew clean assembleRelease

# APK location:
# app/build/outputs/apk/release/app-release-unsigned.apk
```

### APK Analysis

```bash
# Analyze APK size
./gradlew :app:analyzeReleaseApkSize

# Or use Android Studio APK Analyzer:
# Build > Analyze APK > Select app-release-unsigned.apk
```

**Check**:
- Total APK size < 50MB
- ProGuard removed debug logs
- Resources optimized
- Native libraries for target ABIs only

### ProGuard Verification

```bash
# Check ProGuard mapping file
cat app/build/outputs/mapping/release/mapping.txt | head -20

# Verify obfuscation:
# - Classes renamed (a, b, c)
# - Methods renamed (a(), b(), c())
# - BUT: Data models kept (Gson @Keep rules)
```

### Functional Testing

Install release APK on device:

```bash
adb install -r app/build/outputs/apk/release/app-release-unsigned.apk
```

**Test all features**:
- [ ] Map search works
- [ ] Memo CRUD operations
- [ ] Image upload/compression
- [ ] Location services
- [ ] Error handling displays Korean messages
- [ ] No crashes from over-obfuscation

### Crash Log Validation

Trigger intentional crash in release build:

```kotlin
// Add temporarily to MainActivity
throw RuntimeException("Test crash for ProGuard line mapping")
```

**Verify**:
- Stack trace shows file names (proguard keeps SourceFile)
- Line numbers present (keepattributes LineNumberTable)
- Can map obfuscated names via mapping.txt

---

## 6. Battery Performance (Optional)

```bash
# Measure battery usage
adb shell dumpsys batterystats --reset
# Use app for 30 minutes
adb shell dumpsys batterystats com.dailymemo

# Check for:
# - No wakelocks
# - No excessive CPU usage
# - Location updates stop when app backgrounded
```

---

## Benchmark Execution Checklist

### Pre-Benchmark
- [ ] Device connected via ADB
- [ ] Debug build installed
- [ ] LeakCanary dependency added
- [ ] GPU profiling enabled
- [ ] Android Studio Profiler ready

### During Benchmark
- [ ] Record cold start time (3+ runs, average)
- [ ] Profile scroll performance with 100+ items
- [ ] Monitor memory during 10-min session
- [ ] Test image compression with various sizes
- [ ] Build and test release APK

### Post-Benchmark
- [ ] All targets met
- [ ] Results documented
- [ ] Issues filed for any failures
- [ ] Baseline metrics recorded for future regression testing

---

## Expected Results (Pass Criteria)

✅ **Startup**: <2s cold start (avg of 3 runs)
✅ **Scroll**: ≥60fps sustained, <1% dropped frames
✅ **Memory**: No leaks, stable heap growth
✅ **Images**: All <500KB, quality acceptable
✅ **APK Size**: <50MB release build
✅ **Functional**: All features work in release build

**Issue #11 Performance Complete**: All criteria met and documented
