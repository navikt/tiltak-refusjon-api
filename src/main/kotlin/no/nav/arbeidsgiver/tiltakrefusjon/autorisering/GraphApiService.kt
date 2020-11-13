package no.nav.arbeidsgiver.tiltakrefusjon.autorisering

interface GraphApiService {
    fun hent(): GraphApiResponse
    data class GraphApiResponse(val onPremisesSamAccountName: String, val displayName: String)
}