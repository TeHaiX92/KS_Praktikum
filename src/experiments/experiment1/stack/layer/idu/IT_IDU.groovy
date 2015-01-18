package experiments.experiment1.stack.layer.idu

import experiments.experiment1.stack.layer.pdu.PDU

/**
 * IDU von IP zu TCP
 */
class IT_IDU extends IDU {

    /** Quell-IP-Adresse */
    String srcIpAddr

    /** Quell-Port */
    int srcPort

    /** Zu transportierende PDU */
    PDU sdu

    /** Konvertieren in Text */
    String toString() {
        return String.format("IT_IDU: [srcIpAddr: \u001B[32m${srcIpAddr}\u001B[0m, srcPort: \u001B[32m${srcPort}\u001B[0m, sdu: ${sdu}]")
    }
}
