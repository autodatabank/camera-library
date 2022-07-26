package kr.co.kadb.cameralibrary.data.repository

import kr.co.kadb.cameralibrary.data.local.PreferenceManager
import kr.co.kadb.cameralibrary.domain.repository.PreferencesRepository

internal class PreferencesRepositoryImpl(private val preferenceManager: PreferenceManager) :
    PreferencesRepository {
    override var flashMode: Int
        get() = preferenceManager.flashMode
        set(value) {
            preferenceManager.flashMode = value
        }
}