package experiments.experiment1.stack.layer.idu

/**
 * IDU von Anwendung zu UDP
 */
class AU_IDU extends IDU {

    /** Ziel-IP-Adresse */
    String dstIpAddr

    /** Ziel-Port */
    int dstPort

    /** Eigener Port */
    int srcPort

    /** Anwendungsdaten */
    String sdu

    /** Konvertieren in Text */
    String toString() {
        return String.format("AU_IDU: [dstIpAddr: \u001B[32m${dstIpAddr}\u001B[0m, dstPort: \u001B[32m${dstPort}\u001B[0m, srcPort: \u001B[32m${srcPort}\u001B[0m, sdu: ${sdu}]")
    }
}
