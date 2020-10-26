package no.nav.arbeidsgiver.tiltakrefusjon.exceptions

class AltinnFeilException : RuntimeException{
    constructor(message: String?):super(message)
    constructor(message: String?, cause: Throwable?) :super(message, cause)
}