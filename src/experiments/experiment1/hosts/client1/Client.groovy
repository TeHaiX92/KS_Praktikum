#!/usr/bin/env groovy

package experiments.experiment1.hosts.client1

import common.utils.Utils
import experiments.experiment1.stack.Stack
import java.util.regex.Matcher

/**
 * Einfacher HTTP-Client.<br/>
 * Sendet einen HTTP-GET-Request an einen HTTP-Server und gibt das empfangene Dokument an das Terminal aus.<br/>
 * Zum Transport wird UDP verwendet. Der Client ist dadurch nicht für die Kommunikation mit realen HTTP-Servern <br/>
 * verwendbar, da diese stets TCP verwenden!
 */
class Client {

	/** Der Netzwerk-Protokoll-Stack */
	Stack stack

	/** Konfiguration des Geräts.<br/>
	 *  Erster Parameter: der Name des Verzeichnisses, der den Versuch enthält<br/>
	 *  Zweiter Parameter: Name der Konfiguation fuer dieses Gerät in der Konfigurationsdatei
	 */
	ConfigObject config = Utils.getConfig("experiment1", "client")

	/** Stoppen der Threads wenn false */
	Boolean run = true

	/** Ziel-IP-Adresse */
	String serverIpAddr

	/** Zielportadresse */
	int serverPort

	/** Nameserver-IP-Adresse */
	String nameserverIpAddr

	/** Nameserver-Portadresse */
	int nameserverPort

	/** HTTP-Header fuer GET-Request **/
	String request =
		"""\
GET /${config.document} HTTP/1.1


"""

	/** Eigene Portadresse */
	int ownPort

	/** Anwendungs-PDU */
	String apdu

    String srcIpAddr
    int srcPort

	/** Anwendungsprotokolldaten als String */
	String data

	/** Länge des HTTP-Body's */
	int bodyLength = 0

	/** Beginn des HTTP-Body's */
	int bodyStart = 0

	/** Aktuelle Länge des HTTP-Body's */
	int curBodyLength = -1

	// Zustände der Protokollmaschine
	/** Zustand: Datenlänge bestimmen */
	final int WAIT_LENGTH = 100
	/** Zustand: Leerzeile (Double NewLine) feststellen */
	final int WAIT_DNL = 200
	/** Zustand: restliche Daten erwarten */
	final int WAIT_DATA = 300
	/** Zustand der Protokollmaschine */
	int state

	/** Behandlung Regulärer Ausdruecke */
	Matcher matcher

	/** ID der TCP-Verbindung, wenn 0: Fehler */
	int connId = 0

	/**
	 * Start der Anwendung
	 */
	static void main(String[] args) {
		Client application = new Client()
		application.client()
	}

	/**
	 * Ein HTTP-Client mit rudimentärer Implementierung des Protokolls HTTP (Hypertext Transfer Protocol).<br/>
	 * Verwendet das TCP-Protokoll.
	 */
	void client() {

		// Empfangene Daten
		String rdata

		// IPv4-Adresse und Portnummer des HTTP-Dienstes
		serverPort = config.serverPort

		// NameServer
		nameserverIpAddr = config.nameServerIpAddr
		nameserverPort = config.nameServerPort

		// Eigener UDP-Port
		ownPort = config.ownPort

		// Netzwerkstack initialisieren
		stack = new Stack()
		stack.start(config)

		Utils.writeLog("Client", "client", "startet", 1)

		Utils.writeLog("Client", "determine", "ermittle Netzwerk-Adresse \u001B[32m${config.serverName}\u001B[37m", 1)

		// Datenempfang vorbereiten
		data = ""
		state = WAIT_LENGTH

		// HTTP-GET-Request absenden
		stack.udpSend(dstIpAddr: nameserverIpAddr, dstPort: nameserverPort,
			srcPort: ownPort, sdu: config.serverName)

		(srcIpAddr, srcPort, data) = stack.udpReceive()
		/*matcher = (data =~ /ANSWER SECTION:(.*)/)
		serverIpAddr = (matcher[0] as List<String>)[1]
		if (!Utils.isIp(serverIpAddr)) {
			Utils.writeLog("Client", "client", "DNS could not find the ip for the host: $config.serverName", 1)
		}*/
// ----------------------------------------------------------
// HTTP-GET-Request absenden
        serverIpAddr = data
		Utils.writeLog("Client", "send", "sendet: ${request} to $serverIpAddr", 1)
		stack.udpSend(dstIpAddr: serverIpAddr, dstPort: serverPort,
			srcPort: ownPort, sdu: request)

		// Empfang
		while (curBodyLength < bodyLength) {

			// Auf Empfang warten
			String d1, d2
			// dummies
			(d1, d2, rdata) = stack.udpReceive()

			Utils.writeLog("Client", "receiving", "empfängt: $rdata", 1)

			// Daten ergänzen
			data += rdata

			// Empfangene Daten verarbeiten
			handleData()

		} // while

		if (data) Utils.writeLog("Client", "receive", "HTTP-Body empfangen: ${data[bodyStart..-1]}", 1)
	}

	/**
	 * Fügt empfangene Daten zusammen
	 */
	void handleData() {
		if (state == WAIT_LENGTH) {
			// Suchen nach Header-Feld "Content-Length"
			matcher = (data =~ /Content-Length:\s*(\d+)\D/)

			// Wurde das Header-Feld gefunden?
			if (matcher) {
				// Ja
				// Länge des HTTP-Body's holen
				bodyLength = (matcher[0] as List<String>)[1].toInteger()
				state = WAIT_DNL
			}
		}

		if (state == WAIT_DNL) {

			// Suchen nach Leerzeile (HTTP-Header-Ende)
			matcher = (data =~ /\n\n|\r\r|\r\n\r\n/)

			// Wurde die Leerzeile gefunden?
			if (matcher) {
				// Ja, Beginn des HTTP-Body's gefunden

				bodyStart = matcher.start() + 2 // Index (Anfang) des HTTP-Body's
				// Bei UTF-8 Encoding anstatt data.size() besser: data.bytes.size()
				curBodyLength = data.bytes.size() - bodyStart
				state = WAIT_DATA
			}
		} else if (state == WAIT_DATA) {
			curBodyLength = data.bytes.size() - bodyStart
		}
	}
}
