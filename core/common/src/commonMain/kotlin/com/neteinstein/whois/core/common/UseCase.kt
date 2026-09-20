package com.neteinstein.whois.core.common

/**
 * Base type for a use case that transforms [P] into [R].
 * Kept as an abstract class (rather than a fun interface) so Koin can inject
 * collaborators through a constructor like any other class.
 */
abstract class UseCase<in P, out R> {
    abstract suspend operator fun invoke(params: P): R
}

/** Base type for a use case that takes no parameters. */
abstract class NoParamUseCase<out R> {
    abstract suspend operator fun invoke(): R
}
