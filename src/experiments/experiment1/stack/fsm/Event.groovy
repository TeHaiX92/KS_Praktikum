package experiments.experiment1.stack.fsm

/**
 * Definition der möglichen Ereignisse, die zu Zustandänderungen der FSM führen können.
 */
class Event {
	/** Verbindungseröffnung einleiten*/
	static final int E_CONN_REQ = 100

	// NEU
	static final int E_WAIT_REQ = 105

	/** SYN senden */
	static final int E_SEND_SYN = 110

	// NEU
	static final int E_RCVD_SYN = 115

	/** FIN senden */
	static final int E_SEND_FIN = 130

	/** Daten senden */
	static final int E_SEND_DATA = 150

	/** SYN+ACK wurde empfangen */
	static final int E_RCVD_SYN_ACK = 160

	// NEU
	static final int E_SEND_SYN_ACK = 165

	/** Daten wurden empfangen */
	static final int E_RCVD_DATA = 170

	/** ACK wurde empfangen */
	static final int E_RCVD_ACK = 180

	/** ACK zur Verbindungseröffnung wurde gesendet */
	static final int E_SYN_ACK_ACK_SENT = 190

	// NEU
	static final int E_RCVD_SYN_ACK_ACK = 195

	/** FIN+ACK wurde empfangen */
	static final int E_FIN_ACK_ACK_SENT = 210

	/** Bereitschaft */
	static final int E_READY = 220

	/** Verbindungsbeendigung einleiten */
	static final int E_DISCONN_REQ = 230

	//NEU
	static final int E_DISCONN_CON = 231

	// NEU
	static final int E_SEND_FIN_ACK = 235

	/** Daten wurden gesendet */
	static final int E_DATA_SENT = 240

	//NEU
	static final int E_WAIT_FIN_ACK_ACK = 245

	//NEU
	static final int E_RCVD_FIN_ACK_ACK = 255

	/** FIN empfangen */
	static final int E_RCVD_FIN = 280
}