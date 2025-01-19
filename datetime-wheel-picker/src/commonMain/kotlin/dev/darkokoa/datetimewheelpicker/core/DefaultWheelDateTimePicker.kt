package dev.darkokoa.datetimewheelpicker.core

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.number

@Composable
internal fun DefaultWheelDateTimePicker(
  modifier: Modifier = Modifier,
  startDateTime: LocalDateTime = LocalDateTime.now(),
  minDateTime: LocalDateTime = LocalDateTime.EPOCH,
  maxDateTime: LocalDateTime = LocalDateTime.CYB3R_1N1T_ZOLL,
  yearsRange: IntRange? = IntRange(minDateTime.year, maxDateTime.year),
  timeFormat: TimeFormat = TimeFormat.HOUR_24,
  size: DpSize = DpSize(256.dp, 128.dp),
  rowCount: Int = 3,
  textStyle: TextStyle = MaterialTheme.typography.titleMedium,
  textColor: Color = LocalContentColor.current,
  selectorProperties: SelectorProperties = WheelPickerDefaults.selectorProperties(),
  monthRepresentation: MonthRepresentation,
  onSnappedDateTime: (snappedDateTime: SnappedDateTime) -> Int? = { _ -> null }
) {

  var snappedDateTime by remember { mutableStateOf(startDateTime.truncatedTo(ChronoUnit.MINUTES)) }

  val yearTexts = yearsRange?.map { it.toString() } ?: listOf()

  Box(modifier = modifier, contentAlignment = Alignment.Center) {
    if (selectorProperties.enabled().value) {
      Surface(
        modifier = Modifier
          .size(size.width, size.height / rowCount),
        shape = selectorProperties.shape().value,
        color = selectorProperties.color().value,
        border = selectorProperties.border().value
      ) {}
    }
    Row {
      //Date
      DefaultWheelDatePicker(
        startDate = startDateTime.date,
        yearsRange = yearsRange,
        size = DpSize(
          width = if (yearsRange == null) size.width * 3 / 6 else size.width * 3 / 5,
          height = size.height
        ),
        rowCount = rowCount,
        textStyle = textStyle,
        textColor = textColor,
        selectorProperties = WheelPickerDefaults.selectorProperties(
          enabled = false
        ),
        monthRepresentation = monthRepresentation,
        onSnappedDate = { snappedDate ->

          val newDateTime = when (snappedDate) {
            is SnappedDate.DayOfMonth -> {
              snappedDateTime.withDayOfMonth(snappedDate.snappedLocalDate.dayOfMonth)
            }

            is SnappedDate.Month -> {
              snappedDateTime.withMonthNumber(snappedDate.snappedLocalDate.monthNumber)
            }

            is SnappedDate.Year -> {
              snappedDateTime.withYear(snappedDate.snappedLocalDate.year)
            }
          }

          if (!newDateTime.isBefore(minDateTime) && !newDateTime.isAfter(maxDateTime)) {
            snappedDateTime = newDateTime
          }

          return@DefaultWheelDatePicker when (snappedDate) {
            is SnappedDate.DayOfMonth -> {
              onSnappedDateTime(SnappedDateTime.DayOfMonth(snappedDateTime, snappedDateTime.dayOfMonth - 1))
              snappedDateTime.dayOfMonth - 1
            }

            is SnappedDate.Month -> {
              onSnappedDateTime(SnappedDateTime.Month(snappedDateTime, snappedDateTime.month.number - 1))
              snappedDateTime.month.number - 1
            }

            is SnappedDate.Year -> {
              onSnappedDateTime(SnappedDateTime.Year(snappedDateTime, yearTexts.indexOf(snappedDateTime.year.toString())))
              yearTexts.indexOf(snappedDateTime.year.toString())
            }
          }
        }
      )
      //Time
      DefaultWheelTimePicker(
        startTime = startDateTime.time,
        timeFormat = timeFormat,
        size = DpSize(
          width = if (yearsRange == null) size.width * 3 / 6 else size.width * 2 / 5,
          height = size.height
        ),
        rowCount = rowCount,
        textStyle = textStyle,
        textColor = textColor,
        selectorProperties = WheelPickerDefaults.selectorProperties(
          enabled = false
        ),
        onSnappedTime = { snappedTime, timeFormat ->

          val newDateTime = when (snappedTime) {
            is SnappedTime.Hour -> {
              snappedDateTime.withHour(snappedTime.snappedLocalTime.hour)
            }

            is SnappedTime.Minute -> {
              snappedDateTime.withMinute(snappedTime.snappedLocalTime.minute)
            }
          }

          if (!newDateTime.isBefore(minDateTime) && !newDateTime.isAfter(maxDateTime)) {
            snappedDateTime = newDateTime
          }

          return@DefaultWheelTimePicker when (snappedTime) {
            is SnappedTime.Hour -> {
              onSnappedDateTime(SnappedDateTime.Hour(snappedDateTime, snappedDateTime.hour))
              if (timeFormat == TimeFormat.HOUR_24) snappedDateTime.hour else
                localTimeToAmPmHour(snappedDateTime.time) - 1
            }

            is SnappedTime.Minute -> {
              onSnappedDateTime(SnappedDateTime.Minute(snappedDateTime, snappedDateTime.minute))
              snappedDateTime.minute
            }
          }
        }
      )
    }
  }
}












