package experiments.experiment1.hosts.router4

import common.utils.Utils

/**
 * Ein IPv4-Router.<br/>
 * Nur als Ausgangspunkt für eigene Implementierung zu verwenden!<br/>
 * Verwendet UDP zur Verteilung der Routinginformationen.
 *
 */
class Router1 {

	//========================================================================================================
	// Vereinbarungen ANFANG
	//========================================================================================================

	/** Der Netzwerk-Protokoll-Stack */
	experiments.experiment1.stack.Stack stack

	/** Konfigurations-Objekt */
	ConfigObject config

	/** Stoppen der Threads wenn false */
	Boolean run = true

	/** Tabelle der IP-Adressen und UDP-Ports der Nachbarrouter */
	/*  z.B. [["1.2.3.4", 11],["5,6,7.8", 20]]
		*/
	List<List> neighborTable

	/** Tabelle der IP-Adressen, UDP-Ports der Nachbarrouter, Lebensstatus, TTL */
	/*  z.B. [["1.2.3.4", 11, true, 3],["5,6,7.8", 20, false, 0]]
		*/
	List<List> aliveNeighborTable = []

	/** Eine Arbeitskopie der Routingtabelle der Netzwerkschicht */
	List routingTable

	//========================================================================================================
	// Methoden ANFANG
	//========================================================================================================

	//------------------------------------------------------------------------------
	/**
	 * Start der Anwendung
	 */
	static void main(String[] args) {
		Router1 application = new Router1()
		application.router()
	}
	//------------------------------------------------------------------------------

	//------------------------------------------------------------------------------
	/**
	 * Einfacher IP-v4-Forwarder.<br/>
	 * Ist so schon funktiionsfähig, da die Wegewahl im Netzwerkstack erfolgt<br/>
	 * Hier wird im Laufe des Versuchs ein Routing-Protokoll implementiert.
	 */
	void router() {

		// Konfiguration holen
		config = Utils.getConfig("experiment1", "router1")
		neighborTable = config.neighborTable

		// ------------------------------------------------------------

		// Netzwerkstack initialisieren
		stack = new experiments.experiment1.stack.Stack()
		stack.start(config)

		// ------------------------------------------------------------

		// Thread zum Empfang von Routinginformationen erzeugen
		Thread.start{receiveFromNeigbor()}

		// ------------------------------------------------------------

		Utils.writeLog("Router", "router1", "startet", 1)

		sendAliveRequest()

		while (run) {
			// Periodisches Versenden von Routinginformationen
			neighborTTLDec()
			sendPeriodical()
			sleep(config.periodRInfo)
		}
	}

	// ------------------------------------------------------------

	/**
	 * Wartet auf Empfang von Routinginformationen
	 *
	 */
	void receiveFromNeigbor() {

		/** IP-Adresse des Nachbarrouters */
		String iPAddr

		/** UDP-Portnummer des Nachbarrouters */
		int port

		/** Empfangene Routinginformationen */
		String rInfo

		while(run) {

			// Auf UDP-Empfang warten
			(iPAddr, port, rInfo) =  stack.udpReceive()

			if (rInfo.equals("alive")) {

				boolean found = false
				List neighbor = aliveNeighborTable.find { entry -> entry[0].equals(iPAddr) }

				//
				if (neighbor != null) {
					found = true
					neighbor[2] = true
					neighbor[3] = 3 // Time to Live auf 3 Runden
				}

				// Neuer Nachbarn gefunden
				if (!found) {
					List l = [iPAddr, port, true, 3]
					aliveNeighborTable.add(l)
				}
			}
			else {
				// Jetzt aktuelle Routingtablle holen:
				List rt = stack.getRoutingTable()

				// neue Routinginformationen bestimmen

				boolean rt_changed = false
				String[] neighbourRouting = rInfo.tokenize('[')
				List<List> neighbourRoutingTable = []

				neighbourRouting.each { item ->
					item -= ' '
					item -= ']'
					List<String> l = []
					item.tokenize(',').each { str -> l.add(str.trim()) }
					neighbourRoutingTable.add(l)
				}

				// Vergleiche die eigenen Routing-Einträge mit den übermittelten
				// Falls die IP inkl. Maske bisher nicht vorhanden ist, erstelle einen neuen Eintrag

				int toNieghborMetric = 1000
				List dstToNieghborList = rt.find { ownEntry -> // should be findAll
					Utils.getNetworkId(iPAddr, ownEntry[1]).equals(ownEntry[0])
				}

				if (dstToNieghborList != null)
					toNieghborMetric = dstToNieghborList[4].toInteger()


				rt.each { ownEntry ->
					String dstIP = Utils.getNetworkId(ownEntry[0], ownEntry[1])
					int dstIpMetric = ownEntry[4].toInteger()

					// println(Utils.getNetworkId(neighbourRoutingTable[0][1].toString(),neighbourRoutingTable[0][0].toString()))
					List nbDst = neighbourRoutingTable.find { nbEntry -> dstIP.equals(Utils.getNetworkId(ownEntry[1].toString(), nbEntry[0].toString())) }
					boolean isNeighbor = (neighborTable.find{entry -> entry[0].equals(dstIP)}!=null)
					int nbDstMetric = 1000
					if(nbDst!=null&&(!isNeighbor)){
						nbDstMetric = nbDst[4].toInteger()
					}
					if ((nbDstMetric + toNieghborMetric) < dstIpMetric) {
						ownEntry[4] = nbDstMetric + toNieghborMetric
						ownEntry[3] = dstToNieghborList[3]
						ownEntry[2] = iPAddr
						rt_changed = true

						println("router 4 with help from $iPAddr found a better path to $dstIP")
					}
				}

				//    zum Zerlegen einer Zeichenkette siehe "tokenize()"
				// extrahieren von Information, dann iInfo als !Zeichenkette! erzeugen ...

				// Routingtabelle an Vermittlungsschicht uebergeben:
				stack.setRoutingTable(rt)

				// und neue Routinginformationen verteilen:
				String outrInfo = rt.join(",")
				if (rt_changed){
					sendToNeigbors(outrInfo)
					String strout =""
					rt.each{item -> strout += item + "\n"}
					println("the new table of router 4 is:\n $strout")
				}
				// oder periodisch verteilen lassen

			}
		}
	}

	/**
	 * Verringern der Time to Live (TTL)
	 */
	void neighborTTLDec(){
		List rt = stack.getRoutingTable()

		// Untersuchung aller Nachbarn in der ANT
		for(List neighbor in aliveNeighborTable) {

			// Wenn Nachbar noch am Leben, dann TTL verringern
			if (neighbor[3] > 0) {
				neighbor[3]--
			}
			// Ansonsten ist der Nachbar tot
			else {
				rt.each { entry ->
					if (entry[2].toString().equals(neighbor[0])) {
						neighbor[2] = false
					}
				}
			}
		}

		stack.setRoutingTable(rt)
	}
	// ------------------------------------------------------------

	/** Periodisches Senden der Routinginformationen */
	void sendPeriodical() {
		routingTable = stack.getRoutingTable()

		String rInfo = ""

		for (entry in routingTable) {
			rInfo = rInfo + "${entry[0]}, ${entry[1]}, ${entry[2]}, ${entry[3]}; "
		}

		Utils.writeLog("Router", "router1", "rInfo: ${rInfo}",1)

		// Zum Senden uebergeben
		sendToNeigbors(rInfo)
		sendAliveRequest()
	}

	// ------------------------------------------------------------

	/** Senden von Routinginformationen an alle Nachbarrouter
	 *
	 * @param rInfo - Routing-Informationen
	 */
	void sendToNeigbors(String rInfo) {
		// rInfo an alle Nachbarrouter versenden
		for (List neigbor in neighborTable) {
			stack.udpSend(dstIpAddr: neigbor[0], dstPort: neigbor[1], srcPort: config.ownPort, sdu: rInfo)
		}
	}

	//------------------------------------------------------------------------------

	/**
	 * Sende Alive-Request
	 */
	void sendAliveRequest(){
		for (List neigbor in neighborTable) {
			stack.udpSend(dstIpAddr: neigbor[0], dstPort: neigbor[1], srcPort: config.ownPort, sdu: "alive")
		}
	}
}