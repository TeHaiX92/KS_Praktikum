package experiments.experiment1.stack.layer.idu

/**
 * IDU von UDP zu Anwendung
 */
class UA_IDU extends IDU {

    /** Quell-IP-Adresse */
    String srcIpAddr

    /** Quell-Port */
    int srcPort

    /** Anwendungsdaten */
    String sdu

    /** Konvertieren in Text */
    String toString() {
        return String.format("UA_IDU: [srcIpAddr: \u001B[32m${srcIpAddr}\u001B[0m, srcPort: \u001B[32m${srcPort}\u001B[m, sdu: ${sdu}]")
    }
}
