//
//  MockLoginDataSource.swift
//  iosApp
//
//  Fake data source for SwiftUI Previews and UI testing.
//  Mirrors Android's MockLoginDataSource.
//
//  Default behaviour: succeeds after a short delay.
//  Set `shouldFail = true` to exercise the error path.
//

import Foundation
import SharedAuthDomain

final class MockLoginDataSource: NSObject, SharedAuthDomainLoginDataSource {

    var shouldFail  = false
    var delayMs: UInt64 = 600

    func login(email: String, password: String) async throws -> SharedAuthDomainKotlinUnit {
        try await Task.sleep(nanoseconds: delayMs * 1_000_000)

        if shouldFail {
            throw LoginError.serverError("Invalid email or password")
        }
        return SharedAuthDomainKotlinUnit.shared
    }
}
