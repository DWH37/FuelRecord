package com.example.fuelrecord.data

object CsvUtils {

    private const val HEADER = "id,date,totalMileage,fuelAmount,cost,unitPrice,isFull,note"

    fun toCsv(records: List<FuelRecord>): String {
        val sb = StringBuilder()
        sb.appendLine(HEADER)
        for (r in records) {
            val escapedNote = r.note.replace("\"", "\"\"")
            sb.appendLine("${r.id},${r.date},${r.totalMileage},${r.fuelAmount},${r.cost},${r.unitPrice},${r.isFull},\"${escapedNote}\"")
        }
        return sb.toString()
    }

    fun fromCsv(csv: String): List<FuelRecord> {
        val lines = csv.lines()
        if (lines.size < 2) return emptyList()
        return lines.drop(1).filter { it.isNotBlank() }.mapNotNull { line ->
            try {
                val parts = parseLine(line)
                if (parts.size < 8) return@mapNotNull null
                FuelRecord(
                    id = 0, // 导入时重置 id，由数据库自动生成
                    date = parts[1].trim().toLong(),
                    totalMileage = parts[2].trim().toDouble(),
                    fuelAmount = parts[3].trim().toDouble(),
                    cost = parts[4].trim().toDouble(),
                    unitPrice = parts[5].trim().toDouble(),
                    isFull = parts[6].trim().toBoolean(),
                    note = parts[7]
                )
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun parseLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            when {
                c == '"' && !inQuotes -> inQuotes = true
                c == '"' && inQuotes -> {
                    if (i + 1 < line.length && line[i + 1] == '"') {
                        sb.append('"')
                        i++
                    } else {
                        inQuotes = false
                    }
                }
                c == ',' && !inQuotes -> {
                    result.add(sb.toString())
                    sb.clear()
                }
                else -> sb.append(c)
            }
            i++
        }
        result.add(sb.toString())
        return result
    }
}
