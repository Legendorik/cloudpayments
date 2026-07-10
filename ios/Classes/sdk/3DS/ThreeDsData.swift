//
//  ThreeDsData.swift
//  sdk
//
//  Created by Cloudpayments on 25.09.2020.
//  Copyright © 2020 Cloudpayments. All rights reserved.
//

public final class ThreeDsData {
    public private(set) var transactionId: String
    public private(set) var paReq: String
    public private(set) var acsUrl: String
    public private(set) var threeDSCallbackId: String?

    public init(transactionId: String, paReq: String, acsUrl: String, threeDSCallbackId: String? = nil) {
        self.transactionId = transactionId
        self.paReq = paReq
        self.acsUrl = acsUrl
        self.threeDSCallbackId = threeDSCallbackId
    }
}
