package experiments.experiment1.stack.layer.pdu

/**
 * UDP-PDU<br/>
 * Unvollständig
 */
class U_PDU extends PDU  {
    int dstPort
    int srcPort
    String sdu

    String toString() {
        return String.format("UPDU: [dstPort: \u001B[32m${dstPort}\u001B[0m, srcPort: \u001B[32m${srcPort}\u001B[0m, sdu: ${sdu}]")
    }
}

