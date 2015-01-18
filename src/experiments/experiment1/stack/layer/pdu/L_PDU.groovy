package experiments.experiment1.stack.layer.pdu

/**
 * MAC-Frame<br/>
 * Unvollständig
 */
class L_PDU  extends PDU {
    String srcMacAddr
    String dstMacAddr
    int type
    PDU sdu

    String toString() {
        return String.format("L_PDU: [dstMacAddr: \u001B[32m${dstMacAddr}\u001B[0m, srcMacAddr: \u001B[32m${srcMacAddr}\u001B[0m, type: ${type}, sdu: ${sdu}]")
    }
}
