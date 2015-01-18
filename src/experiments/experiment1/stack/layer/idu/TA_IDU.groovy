package experiments.experiment1.stack.layer.idu

/**
 * IDU von TCP zu Anwendung
 */
class TA_IDU extends IDU {

    /** ID der Verbindung */
    int connId

    /** Quell-IP-Adresse */
    String srcIpAddr

    /** Quell-Port */
    int srcPort

    /** Anwendungsdaten */
    String sdu

    /** Konvertieren in Text */
    String toString() {
        return String.format("TU_IDU: [srcIpAddr: \u001B[32m${srcIpAddr}\u001B[0m, srcPort: \u001B[32m${srcPort}\u001B[0m, sdu: ${sdu}]")
    }
}
