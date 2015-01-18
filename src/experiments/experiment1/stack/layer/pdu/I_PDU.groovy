package experiments.experiment1.stack.layer.pdu

/**
 * IP-PDU<br/>
 * Unvollständig
 */
class I_PDU extends PDU {
    String dstIpAddr
    String srcIpAddr
    int offset
    int id
    int protocol
    PDU sdu

    String toString() {
        return String.format("I_PDU: [dstIpAddr: \u001B[32m${dstIpAddr}\u001B[0m, srcIpAddr: \u001B[32m${srcIpAddr}\u001B[0m, protocol: ${protocol}, sdu: ${sdu}]")
    }
}
