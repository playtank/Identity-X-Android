//
//  MockLoginDataSource.swift
//  iosApp
//
//  Fake LoginDataSource for SwiftUI Previews and UI testing.
//  Set shouldFail = true to exercise the error path.
//
//  KMP suspend fun login() bridges as a completion handler in Swift,
//  not as async throws.
//

import Foundation
import SharedAuthDomain

final class MockLoginDataSource: NSObject, LoginDataSource {

    var shouldFail = false
    var delayMs: UInt64 = 600

    func login(
        email: String,
        password: String,
        completionHandler: @escaping (Any?, Error?) -> Void
    ) {
        DispatchQueue.main.asyncAfter(deadline: .now() + .milliseconds(Int(delayMs))) {
            if self.shouldFail {
                completionHandler(
                    nil,
                    NSError(domain: "MockLogin", code: 401,
                            userInfo: [NSLocalizedDescriptionKey: "Invalid email or password"])
                )
            } else {
                completionHandler(KotlinUnit.shared, nil)
            }
        }
    }
}
