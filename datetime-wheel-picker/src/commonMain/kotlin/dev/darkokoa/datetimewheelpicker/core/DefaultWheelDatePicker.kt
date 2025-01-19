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
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.number

@Composable
internal fun DefaultWheelDatePicker(
  modifier: Modifier = Modifier,
  startDate: LocalDate = LocalDate.now(),
  minDate: LocalDate = LocalDate.EPOCH,
  maxDate: LocalDate = LocalDate.CYB3R_1N1T_ZOLL,
  yearsRange: IntRange? = IntRange(minDate.year, maxDate.year),
  size: DpSize = DpSize(256.dp, 128.dp),
  rowCount: Int = 3,
  textStyle: TextStyle = MaterialTheme.typography.titleMedium,
  textColor: Color = LocalContentColor.current,
  selectorProperties: SelectorProperties = WheelPickerDefaults.selectorProperties(),
  monthRepresentation: MonthRepresentation,
  onSnappedDate: (snappedDate: SnappedDate) -> Int? = { _ -> null }
) {
  val itemCount = if (yearsRange == null) 2 else 3
  val itemWidth = size.width / itemCount

  var snappedDate by remember { mutableStateOf(startDate) }

  var dayOfMonths = calculateDayOfMonths(snappedDate.month.number, snappedDate.year)

  val months = (1..12).map {
    val monthName = monthRepresentation.toMonthName(
      month = Month(it),
      dpWidthSize = size.width
    )

    Month(
      text = monthName,
      value = it,
      index = it - 1
    )
  }

  val years = yearsRange?.map {
    Year(
      text = it.toString(),
      value = it,
      index = yearsRange.indexOf(it)
    )
  }

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
      //Day of Month
      WheelTextPicker(
        size = DpSize(
          width = itemWidth,
          height = size.height
        ),
        texts = dayOfMonths.map { it.text },
        rowCount = rowCount,
        style = textStyle,
        color = textColor,
        selectorProperties = WheelPickerDefaults.selectorProperties(
          enabled = false
        ),
        startIndex = dayOfMonths.find { it.value == startDate.dayOfMonth }?.index ?: 0,
        onScrollFinished = { snappedIndex ->

          val newDayOfMonth = dayOfMonths.find { it.index == snappedIndex }?.value

          newDayOfMonth?.let {
            val newDate = snappedDate.withDayOfMonth(newDayOfMonth)

            if (!newDate.isBefore(minDate) && !newDate.isAfter(maxDate)) {
              snappedDate = newDate
            }

            val newIndex = dayOfMonths.find { it.value == snappedDate.dayOfMonth }?.index

            newIndex?.let {
              onSnappedDate(
                SnappedDate.DayOfMonth(
                  localDate = snappedDate,
                  index = newIndex
                )
              )?.let { return@WheelTextPicker it }
            }
          }

          return@WheelTextPicker dayOfMonths.find { it.value == snappedDate.dayOfMonth }?.index
        }
      )
      //Month
      WheelTextPicker(
        size = DpSize(
          width = itemWidth,
          height = size.height
        ),
        texts = months.map { it.text },
        rowCount = rowCount,
        style = textStyle,
        color = textColor,
        selectorProperties = WheelPickerDefaults.selectorProperties(
          enabled = false
        ),
        startIndex = months.find { it.value == startDate.monthNumber }?.index ?: 0,
        onScrollFinished = { snappedIndex ->

          val newMonth = months.find { it.index == snappedIndex }?.value

          newMonth?.let {

            val newDate = snappedDate.withMonthNumber(newMonth)

            if (!newDate.isBefore(minDate) && !newDate.isAfter(maxDate)) {
              snappedDate = newDate
            }

            dayOfMonths = calculateDayOfMonths(snappedDate.month.number, snappedDate.year)

            val newIndex = months.find { it.value == snappedDate.monthNumber }?.index

            newIndex?.let {
              onSnappedDate(
                SnappedDate.Month(
                  localDate = snappedDate,
                  index = newIndex
                )
              )?.let { return@WheelTextPicker it }
            }
          }


          return@WheelTextPicker months.find { it.value == snappedDate.monthNumber }?.index
        }
      )
      //Year
      years?.let { years ->
        WheelTextPicker(
          size = DpSize(
            width = itemWidth,
            height = size.height
          ),
          texts = years.map { it.text },
          rowCount = rowCount,
          style = textStyle,
          color = textColor,
          selectorProperties = WheelPickerDefaults.selectorProperties(
            enabled = false
          ),
          startIndex = years.find { it.value == startDate.year }?.index ?: 0,
          onScrollFinished = { snappedIndex ->

            val newYear = years.find { it.index == snappedIndex }?.value

            newYear?.let {

              val newDate = snappedDate.withYear(newYear)

              if (!newDate.isBefore(minDate) && !newDate.isAfter(maxDate)) {
                snappedDate = newDate
              }

              dayOfMonths = calculateDayOfMonths(snappedDate.month.number, snappedDate.year)

              val newIndex = years.find { it.value == snappedDate.year }?.index

              newIndex?.let {
                onSnappedDate(
                  SnappedDate.Year(
                    localDate = snappedDate,
                    index = newIndex
                  )
                )?.let { return@WheelTextPicker it }

              }
            }

            return@WheelTextPicker years.find { it.value == snappedDate.year }?.index
          }
        )
      }
    }
  }
}

internal data class DayOfMonth(
  val text: String,
  val value: Int,
  val index: Int
)

private data class Month(
  val text: String,
  val value: Int,
  val index: Int
)

private data class Year(
  val text: String,
  val value: Int,
  val index: Int
)

internal fun calculateDayOfMonths(month: Int, year: Int): List<DayOfMonth> {

  val isLeapYear = LocalDate(year, month, 1).isLeapYear

  val month31day = (1..31).map {
    DayOfMonth(
      text = it.toString(),
      value = it,
      index = it - 1
    )
  }
  val month30day = (1..30).map {
    DayOfMonth(
      text = it.toString(),
      value = it,
      index = it - 1
    )
  }
  val month29day = (1..29).map {
    DayOfMonth(
      text = it.toString(),
      value = it,
      index = it - 1
    )
  }
  val month28day = (1..28).map {
    DayOfMonth(
      text = it.toString(),
      value = it,
      index = it - 1
    )
  }

  return when (month) {
    1 -> {
      month31day
    }

    2 -> {
      if (isLeapYear) month29day else month28day
    }

    3 -> {
      month31day
    }

    4 -> {
      month30day
    }

    5 -> {
      month31day
    }

    6 -> {
      month30day
    }

    7 -> {
      month31day
    }

    8 -> {
      month31day
    }

    9 -> {
      month30day
    }

    10 -> {
      month31day
    }

    11 -> {
      month30day
    }

    12 -> {
      month31day
    }

    else -> {
      emptyList()
    }
  }
}