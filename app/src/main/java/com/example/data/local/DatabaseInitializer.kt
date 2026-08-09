package com.example.data.local

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DatabaseInitializer {

    suspend fun seedSampleDataIfNeeded(db: GymDatabase) = withContext(Dispatchers.IO) {
        val membershipDao = db.membershipDao()

        // Check if database is already populated with default tariffs
        val existingType = membershipDao.getMembershipTypeById(1)
        if (existingType != null) return@withContext

        val mTypes = listOf(
            MembershipTypeEntity(id = 1, name = "12 Занятий (1 Месяц)", durationType = "visits", durationValue = 12, price = 3500.0),
            MembershipTypeEntity(id = 2, name = "Безлимит 1 Месяц", durationType = "days", durationValue = 30, price = 4800.0),
            MembershipTypeEntity(id = 3, name = "Безлимит 3 Месяца", durationType = "days", durationValue = 90, price = 12500.0),
            MembershipTypeEntity(id = 4, name = "8 Занятий (Студенческий)", durationType = "visits", durationValue = 8, price = 2400.0),
            MembershipTypeEntity(id = 5, name = "Годовой Персональный", durationType = "days", durationValue = 365, price = 39000.0)
        )
        mTypes.forEach { membershipDao.insertMembershipType(it) }

        // Seed Default Currency
        val currencyDao = db.currencyDao()
        currencyDao.insertCurrency(CurrencyEntity(id = 1, name = "Сомони", code = "TJS", isSelected = true))
    }
}
