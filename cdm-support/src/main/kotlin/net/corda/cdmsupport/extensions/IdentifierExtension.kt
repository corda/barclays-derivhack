package net.corda.cdmsupport.extensions

import org.isda.cdm.Identifier

fun Identifier.sameTrade(identifier: Identifier): Boolean {
    if (!sameIdentifierAndIdentifierScheme(identifier)) {
        return false
    }

    return this.issuer == identifier.issuer
}

private fun Identifier.sameIdentifierAndIdentifierScheme(identifier: Identifier): Boolean {
    return this.assignedIdentifier == identifier.assignedIdentifier
            && this.meta.scheme == identifier.meta.scheme
}
