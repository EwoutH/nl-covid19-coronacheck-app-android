package nl.rijksoverheid.ctr.verifier.usecases

import clmobile.Clmobile
import com.squareup.moshi.Moshi
import nl.rijksoverheid.ctr.shared.ext.toObject
import nl.rijksoverheid.ctr.shared.ext.verify
import nl.rijksoverheid.ctr.api.models.DecryptedQr
import nl.rijksoverheid.ctr.api.models.TestResultAttributes
import nl.rijksoverheid.ctr.shared.util.CryptoUtil
import timber.log.Timber
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class DecryptHolderQrUseCase(private val moshi: Moshi) {

    fun decrypt(
        content: String
    ): DecryptResult {
        try {
            val result =
                Clmobile.verifyQREncoded(
                    CryptoUtil.ISSUER_PK_XML.toByteArray(),
                    content.toByteArray()
                )
                    .verify()
            Timber.i("QR Code created at ${result.unixTimeSeconds}")
            val testResultAttributes =
                result.attributesJson.decodeToString().toObject<nl.rijksoverheid.ctr.api.models.TestResultAttributes>(moshi)
            return DecryptResult.Success(
                nl.rijksoverheid.ctr.api.models.DecryptedQr(
                    creationDate = OffsetDateTime.ofInstant(
                        Instant.ofEpochSecond(result.unixTimeSeconds),
                        ZoneOffset.UTC
                    ),
                    sampleDate = OffsetDateTime.ofInstant(
                        Instant.ofEpochSecond(testResultAttributes.sampleTime),
                        ZoneOffset.UTC
                    ),
                    testType = testResultAttributes.testType
                )
            )
        } catch (e: Exception) {
            return DecryptResult.Failed
        }
    }

    sealed class DecryptResult {
        class Success(val decryptQr: nl.rijksoverheid.ctr.api.models.DecryptedQr) : DecryptResult()
        object Failed : DecryptResult()
    }
}
