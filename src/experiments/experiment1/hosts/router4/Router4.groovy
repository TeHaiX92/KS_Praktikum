package experiments.experiment1.hosts.router4
import common.utils.Utils
/**
 * Ein IPv4-Router.<br/>
 * Nur als Ausgangspunkt für eigene Implementierung zu verwenden!<br/>
 * Verwendet UDP zur Verteilung der Routinginformationen.
 *
 */
class Router4 {
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
/* z.B. [["1.2.3.4", 11],["5,6,7.8", 20]]
*/
    List<List> neighborTable
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
// Router-Klasse instanziieren
        Router4 application = new Router4()
// und starten
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
        config = Utils.getConfig("experiment1", "router4")
// ------------------------------------------------------------
// Netzwerkstack initialisieren
        stack = new experiments.experiment1.stack.Stack()
        stack.start(config)
// ------------------------------------------------------------
        neighborTable = config.neighborTable
// Thread zum Empfang von Routinginformationen erzeugen
//Thread.start{receiveFromNeigbor()}
// ------------------------------------------------------------
        Utils.writeLog("Router4", "router4", "startet", 1)
        while (run) {
// Periodisches Versenden von Routinginformationen
            sleep(config.periodRInfo)
//sendPeriodical()
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
        while (run) {
// Auf UDP-Empfang warten
            (iPAddr, port, rInfo) = stack.udpReceive()
            List iInfo = rInfo.tokenize()
// Jetzt aktuelle Routingtablle holen:
            List<List> rt = stack.getRoutingTable()
            List<List> copyrt = rt.clone() as List<List>
            for (entry in rt) {
                for (info in iInfo) {
                    if (entry[0].equals(info[0])) {
                        if (!entry[2].equals(iPAddr)) {
                            entry.putAt(2, iPAddr)
                        }
                    }
                }
            }
            stack.setRoutingTable(copyrt)
        }
    }
// ------------------------------------------------------------
/** Periodisches Senden der Routinginformationen */
    void sendPeriodical() {
// Paket mit Routinginformationen packen
// Routingtabelle holen
        routingTable = stack.getRoutingTable()
// extrahieren von Information, dann rInfo als Zeichenkette
        String rInfo="";
        List routingEntry;
        for (int i=0; i<routingTable.size(); i++){
            routingEntry = routingTable.get(i)
            if (i == routingTable.size()-1){
                rInfo=rInfo + routingEntry.get(i)
            }else{
                rInfo=rInfo + routingEntry.get(0) + " "
            }
        }
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
            stack.udpSend(dstIpAddr: neigbor[0], dstPort: neigbor[1],
                    srcPort: config.ownPort, sdu: rInfo)
        }
    }
//------------------------------------------------------------------------------
}