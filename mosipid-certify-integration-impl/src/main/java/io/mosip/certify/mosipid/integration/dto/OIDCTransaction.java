/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.mosip.certify.mosipid.integration.dto;

import lombok.Data;
import java.io.Serializable;


@Data
public class OIDCTransaction implements Serializable {

    String transactionId;
    String relyingPartyId;
    String[] claimsLocales;
    String authTransactionId;
    String kycToken;
    String individualId;

}
