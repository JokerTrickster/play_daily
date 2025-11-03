package com.dailymemo.presentation.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import androidx.core.content.ContextCompat
import com.dailymemo.R

/**
 * 말풍선 스타일 마커 비트맵 생성 헬퍼
 */
object MarkerBitmapHelper {

    /**
     * 말풍선 스타일 마커 비트맵 생성
     * @param context Android Context
     * @param title 메모 제목
     * @param rating 평점 (0이면 표시 안함)
     * @param color 말풍선 배경 색상
     * @return 말풍선 모양의 비트맵
     */
    fun createSpeechBubbleMarker(
        context: Context,
        title: String,
        rating: Float,
        color: Int
    ): Bitmap {
        // 텍스트 준비
        val displayText = if (rating > 0) {
            "★ ${String.format("%.1f", rating)} $title"
        } else {
            title
        }

        // 텍스트가 너무 길면 자르기
        val maxLength = 15
        val truncatedText = if (displayText.length > maxLength) {
            displayText.substring(0, maxLength) + "..."
        } else {
            displayText
        }

        // Paint 설정
        val textPaint = Paint().apply {
            isAntiAlias = true
            textSize = 28f
            setColor(android.graphics.Color.WHITE)
            textAlign = Paint.Align.LEFT
            isFakeBoldText = true
        }

        val backgroundPaint = Paint().apply {
            isAntiAlias = true
            setColor(color)
            style = Paint.Style.FILL
        }

        val strokePaint = Paint().apply {
            isAntiAlias = true
            setColor(android.graphics.Color.WHITE)
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }

        // 텍스트 크기 측정
        val textBounds = android.graphics.Rect()
        textPaint.getTextBounds(truncatedText, 0, truncatedText.length, textBounds)

        // 말풍선 크기 계산
        val padding = 16f
        val bubbleWidth = textBounds.width() + padding * 2
        val bubbleHeight = textBounds.height() + padding * 2
        val tailHeight = 12f
        val cornerRadius = 8f

        // 비트맵 생성
        val bitmap = Bitmap.createBitmap(
            bubbleWidth.toInt(),
            (bubbleHeight + tailHeight).toInt(),
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)

        // 말풍선 Path 생성 (둥근 사각형 + 아래 꼬리)
        val path = Path().apply {
            // 둥근 사각형
            addRoundRect(
                RectF(0f, 0f, bubbleWidth, bubbleHeight),
                cornerRadius,
                cornerRadius,
                Path.Direction.CW
            )

            // 아래쪽 꼬리 (삼각형)
            val tailWidth = 16f
            val centerX = bubbleWidth / 2
            moveTo(centerX - tailWidth / 2, bubbleHeight)
            lineTo(centerX, bubbleHeight + tailHeight)
            lineTo(centerX + tailWidth / 2, bubbleHeight)
            close()
        }

        // 말풍선 그리기
        canvas.drawPath(path, backgroundPaint)
        canvas.drawPath(path, strokePaint)

        // 텍스트 그리기
        val textX = padding
        val textY = padding + textBounds.height()
        canvas.drawText(truncatedText, textX, textY, textPaint)

        return bitmap
    }

    /**
     * 원형 마커 비트맵 생성 (현재 위치 표시용)
     * @param size 원의 크기 (픽셀)
     * @param color 원의 색상
     * @param strokeColor 테두리 색상
     * @param strokeWidth 테두리 두께
     * @return 원형 비트맵
     */
    fun createCircleMarker(
        size: Int,
        color: Int,
        strokeColor: Int = android.graphics.Color.WHITE,
        strokeWidth: Float = 4f
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val centerX = size / 2f
        val centerY = size / 2f
        val radius = (size - strokeWidth) / 2f

        // Fill paint
        val fillPaint = Paint().apply {
            isAntiAlias = true
            setColor(color)
            style = Paint.Style.FILL
        }

        // Stroke paint
        val strokePaint = Paint().apply {
            isAntiAlias = true
            setColor(strokeColor)
            style = Paint.Style.STROKE
            this.strokeWidth = strokeWidth
        }

        // Draw circle with fill and stroke
        canvas.drawCircle(centerX, centerY, radius, fillPaint)
        canvas.drawCircle(centerX, centerY, radius, strokePaint)

        return bitmap
    }

    /**
     * 카테고리별 색상 가져오기
     */
    fun getCategoryColor(categoryName: String): Int {
        return when (categoryName) {
            "RESTAURANT" -> android.graphics.Color.parseColor("#FF3B30")  // 선명한 빨강
            "CAFE" -> android.graphics.Color.parseColor("#A0522D")        // 시에나 브라운
            "SHOPPING" -> android.graphics.Color.parseColor("#AF52DE")    // 보라
            "ACCOMMODATION" -> android.graphics.Color.parseColor("#007AFF") // 밝은 파랑
            "CULTURE" -> android.graphics.Color.parseColor("#FF2D55")     // 핑크
            "LEISURE" -> android.graphics.Color.parseColor("#34C759")     // 밝은 초록
            "TRAVEL" -> android.graphics.Color.parseColor("#FF9500")      // 주황
            "OTHER" -> android.graphics.Color.parseColor("#8E8E93")       // 회색
            else -> android.graphics.Color.parseColor("#FF3B30")
        }
    }
}
