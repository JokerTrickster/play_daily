package com.dailymemo.benchmark

import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Benchmark for app startup time
 * Target: < 2 seconds cold start
 */
@RunWith(AndroidJUnit4::class)
class StartupBenchmark {

    @get:Rule
    val benchmarkRule = BenchmarkRule()

    @Test
    fun startupBenchmark() {
        benchmarkRule.measureRepeated {
            val instrumentation = InstrumentationRegistry.getInstrumentation()

            // Measure time from cold start to first frame
            val startTime = System.currentTimeMillis()

            instrumentation.targetContext.packageManager.getLaunchIntentForPackage(
                instrumentation.targetContext.packageName
            )?.let {
                instrumentation.context.startActivity(it.apply {
                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK)
                })
            }

            // Wait for first frame
            instrumentation.waitForIdleSync()

            val endTime = System.currentTimeMillis()
            val duration = endTime - startTime

            // Assert < 2000ms target
            assert(duration < 2000) {
                "Startup time $duration ms exceeds 2000ms target"
            }
        }
    }
}
