package experiments.experiment1.hosts.router3

import common.utils.Utils

/**
 * Ein IPv4-Router.<br/>
 * Nur als Ausgangspunkt für eigene Implementierung zu verwenden!<br/>
 * Verwendet UDP zur Verteilung der Routinginformationen.
 *
 */
class Router3 {

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
        Router3 application = new Router3()
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
        config = Utils.getConfig("experiment1", "router3")
        neighborTable = config.neighborTable

        // Nachbarn in ANT übernehmen mit false und 0

        int i = 0
        for(List entry in neighborTable) {
            List neighbor = [entry[0],entry[1],false,0,i++]
            aliveNeighborTable.add(neighbor)
        }

        printANT()

        // ------------------------------------------------------------

        // Netzwerkstack initialisieren
        stack = new experiments.experiment1.stack.Stack()
        stack.start(config)

        // ------------------------------------------------------------

        // Thread zum Empfang von Routinginformationen erzeugen
        Thread.start{receiveFromNeigbor()}

        // ------------------------------------------------------------

        Utils.writeLog("Router", "router1", "startet", 1)

        while (run) {

            sendAliveRequest()
            getAliveRequest()

            getAliveReply()
            // TTL verringern
            decreaseNeighborTTL()

            // Periodisches Versenden von Routinginformationen
            sendPeriodical()

            sleep(config.periodRInfo)

            // Status anfragen
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

        // Auf UDP-Empfang warten
        (iPAddr, port, rInfo) =  stack.udpReceive()
    }

    // ------------------------------------------------------------

    /** Periodisches Senden der Routinginformationen */
    void sendPeriodical() {
        routingTable = stack.getRoutingTable()

        String rInfo = ""

        for (List entry in routingTable) {
            rInfo = rInfo + "${entry[0]}, ${entry[1]}, ${entry[2]}, ${entry[3]}; "
        }

        Utils.writeLog("Router", "router1", "rInfo: ${rInfo}",1)

        // Zum Senden uebergeben
        sendToNeigbors(rInfo)
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

    void printANT() {
        for(entry in aliveNeighborTable) {
            println("IP: ${entry[0]}, Port: ${entry[1]}, isAlive: ${entry[2]}, TTL: ${entry[3]}, Pos: ${entry[4]}")
        }
    }

    void sendAliveRequest() {
        for (List neigbor in neighborTable) {
            //Utils.writeLog("Router 1", "send", "\u001B[32mAliveRequest\u001B[0m", 1)
            println("\u001B[32mAliveRequest\u001B[0m")
            stack.udpSend(dstIpAddr: neigbor[0], dstPort: neigbor[1], srcPort: config.ownPort, sdu: "alive")
        }
    }

    void getAliveRequest() {
        /** IP-Adresse des Nachbarrouters */
        String iPAddr

        /** UDP-Portnummer des Nachbarrouters */
        int port

        /** Empfangene Routinginformationen */
        String rInfo

        // Auf UDP-Empfang warten
        (iPAddr, port, rInfo) =  stack.udpReceive()

        if (rInfo.equals("alive")) {
            Utils.writeLog("Router 1", "receive", "\u001B[32mAliveRequest\u001B[0m", 1)
            sendAliveReply(iPAddr, port)
        }
    }

    void sendAliveReply(String IpAddr, int port) {
        stack.udpSend(dstIpAddr: IpAddr, dstPort: port, srcPort: config.ownPort, sdu: "ok")
        Utils.writeLog("Router 1", "send", "\u001B[32mAliveReply\u001B[0m", 1)
    }

    void getAliveReply() {
        String iPAddr
        int port
        String rInfo

        // Auf UDP-Empfang warten
        (iPAddr, port, rInfo) =  stack.udpReceive()

        if (rInfo.equals("ok")) {

            // Nachbarrouter ausfindig machen
            List neighbor = aliveNeighborTable.find { entry -> entry[0].equals(iPAddr) }

            // Nachbarrouter aktualisieren
            if (neighbor != null) {
                neighbor[2] = true
                neighbor[3] = 3 // Time to Live auf 3 Runden
            }

            int i = (int) neighbor[4]

            // ANT aktualisieren
            aliveNeighborTable.set(i,neighbor)

            printANT()
        }
    }

    /**
     * Verringern der Time to Live (TTL)
     */
    void decreaseNeighborTTL() {
        // Untersuchung aller Nachbarn in der ANT
        for(List neighbor in aliveNeighborTable) {

            // Wenn Nachbar noch am Leben, dann TTL verringern
            if (neighbor[3] > 0) {
                neighbor[3]--
            }
            // Ansonsten ist der Nachbar tot
            else {
                neighbor[2] = false
            }

            int i = (int) neighbor[4]

            aliveNeighborTable.set(i,neighbor)

            printANT()
        }
    }
}