package experiments.experiment1.hosts.nameserver

import common.utils.Utils

/**
 * Ein Server der Gerätenamen in IPv4-Adressen auflöst. Als Transport-Protokoll wird UDP verwendet.
 */
class NameServer {

	/** Der Netzwerk-Protokoll-Stack */
	experiments.experiment1.stack.Stack stack

	/** Konfigurations-Objekt */
	ConfigObject config

	/** Stoppen der Threads wenn false */
	Boolean run = true

	/** Tabelle zur Umsetzung von Namen in IP-Adressen */
	Map<String, String> nameTable = [
		"meinhttpserver": "192.168.2.10"
	]

	String srcIpAddr
	int srcPort
	String data

	/**
	 * Start der Anwendung
	 */
	static void main(String[] args) {
		NameServer application = new NameServer()
		application.nameserver()
	}

	/**
	 * Der Namens-Dienst
	 */
	void nameserver() {

		// Konfiguration des Nameservers holen
		config = Utils.getConfig("experiment1", "nameserver")

		// Netzwerkstack initialisieren
		stack = new experiments.experiment1.stack.Stack()
		stack.start(config)

		// Name
		Utils.writeLog("Nameserver", "nameserver", "startet", 1)

		while (run) {
			// auf Empfang ueber UDP warten
			(srcIpAddr, srcPort, data) = stack.udpReceive()

			// Namen über nameTable in IP-Adresse aufloesen
			Utils.writeLog("Nameserver", "receive", "DNS-Anfrage zur Aufloesung des Hosts \u001B[36m${data}\u001B[0m wurde erhalten", 1)


			String ipAddr = nameTable.get(data)
			if (ipAddr) {
				Utils.writeLog("Nameserver", "solve", "DNS-Anfrage aufgeloest in \u001B[36m$ipAddr\u001B[0m", 1)
			} else {
				Utils.writeLog("Nameserver", "error", "DNS konnte die IP-Adresse fuer den Host \u001B[36m$data\u001B[0m nicht finden", 1)
				ipAddr = "0.0.0.0"
			}

			// IP-Adresse ueber UDP zuruecksenden
			stack.udpSend(dstIpAddr: srcIpAddr, dstPort: srcPort, srcPort: config.ownPort, sdu: ipAddr)
		}
	}
}
