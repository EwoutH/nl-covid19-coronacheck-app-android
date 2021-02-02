package nl.rijksoverheid.ctr.holder

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import nl.rijksoverheid.ctr.holder.usecases.HolderQrCodeUseCase
import nl.rijksoverheid.ctr.holder.usecases.SecretKeyUseCase
import nl.rijksoverheid.ctr.shared.models.Result

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class HolderViewModel(
    private val secretKeyUseCase: SecretKeyUseCase,
    private val holderQrCodeUseCase: HolderQrCodeUseCase
) : ViewModel() {

    val qrCodeLiveData = MutableLiveData<Result<Bitmap>>()

    init {
        viewModelScope.launch {
            secretKeyUseCase.persist()
        }
    }

    fun generateQrCode(activity: AppCompatActivity, qrCodeWidth: Int, qrCodeHeight: Int) {
        viewModelScope.launch {
            try {
                val qrCodeBitmap = holderQrCodeUseCase.qrCode(
                    activity = activity,
                    qrCodeWidth = qrCodeWidth,
                    qrCodeHeight = qrCodeHeight
                )
                qrCodeLiveData.postValue(Result.Success(qrCodeBitmap))
            } catch (e: Exception) {
                qrCodeLiveData.postValue(Result.Failed(e))
            }
        }
    }
}
