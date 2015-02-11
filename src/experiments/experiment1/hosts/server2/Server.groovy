#!/usr/bin/env groovy

package experiments.experiment1.hosts.server2

import common.utils.Utils
import experiments.experiment1.stack.Stack
import java.util.regex.Matcher

/**
 * Ein einfacher HTTP-Server.<br/>
 * Liefert bei Übergabe des Dokumentsnamen "index.html" den Namen des angeforderten Dokuments zurück.<br/>
 * Soll bei Übergabe des Dokumentsnamens "daten" eine größere Datenmenge zu Testzwecken liefern.
 */
class Server {

    //========================================================================================================
    // Vereinbarungen ANFANG
    //========================================================================================================

    // Der Netzwerk-Protokoll-Stack
    Stack stack

    /** Konfigurations-Objekt */
    ConfigObject config

    /** Stoppen der Threads wenn false */
    Boolean run = true
    /** ID der TCP-Verbindung */
    int connId

    /** Der im HTTP-Request gelieferte Name */
    String name = ""

    /** Anwendungsprotokolldaten */
    String apdu

    /** Anwendungsprotokolldaten als String */
    String data

    /** Länge der gesendeten Daten */
    int dataLength = 0

    /** Antwort */
    GString reply1 =
            """\
HTTP/1.1 200 OK
Content-Length: ${->dataLength}
Content-Type: text/plain

"""

    GString reply2 =
            """\
Das Objekt ${->name} wurde angefragt!
"""

    /** Ein Matcher-Objekt zur Verwendung regulärer Ausdruecke */
    Matcher matcher

    /** Daten empfangen solange false */
    boolean ready = false


    //========================================================================================================
    // Methoden ANFANG
    // ========================================================================================================

	String nameService(String name) {
		String ip
		switch(name) {
			case "meinhttpserver": 	ip = "192.168.2.10"
						break
			default:		ip = ""
						break
		}
		return ip
	}

    //------------------------------------------------------------------------------
    /**
     * Start der Anwendung
     */
    static void main(String[] args) {
        // Client-Klasse instanziieren
        Server application = new Server()
        // und starten
        application.server()
    }

    //------------------------------------------------------------------------------

    //------------------------------------------------------------------------------
    /**
     * Ein HTTP-Server mit rudimentärer Implementierung des Protokolls HTTP (Hypertext Transfer Protocol)
     */
    void server() {

        // ------------------------------------------------------------

        // Konfiguration holen
        config = Utils.getConfig("experiment1", "server")

        // ------------------------------------------------------------

        // Netzwerkstack initialisieren
        stack = new Stack()
        stack.start(config)

        //------------------------------------------------

        Utils.writeLog("Server", "server", "startet", 1)


        //------------------------------------------------

        while (run) {
            // Auf das Öffnen einer TCP-Verbindung warten
            Map aidu = stack.tcpListen()

            // Verbindungskennung merken
            connId = aidu.connId

            while (run) {

                // Auf Empfang warten
                Map tidu = stack.tcpReceive(connId: connId)

                // Es wurden längere Zeit keine Daten empfangen oder die Datenlänge ist 0
                // -> die TCP-Verbindung wird als geschlossen angenommen
                if (!tidu.sdu)
                // Nein, innere while-Schleife abbrechen
                    break

                // A-PDU uebernehmen
                apdu = tidu.sdu

                Utils.writeLog("Server", "server", "empfängt: ${new String(apdu)}", 1)

                // Protokollkopf holen
                data = new String(apdu)

                // Parsen des HTTP-Kommandos
                matcher = (data =~ /GET\s*\/(.*?)\s*HTTP\/1\.1/)

                name = ""
                // Wurde das Header-Feld gefunden?
                if (matcher) {
                    // Ja
                    // Name des zu liefernden Objekts
                    name = (matcher[0] as List<String>)[1]

                    String reply = ""

                    switch (name) {
                        case "index.html":
                            // Antwort erzeugen
                            String temp = reply2 // name wird eingetragen
                            dataLength = reply2.size()
                            reply = reply1 + temp // dabei wird dataLength in reply1 eingetragen
                            break

                        case "daten":
                            // hier langen HTTP-body erzeugen um lang anhaltende Übertragung zu erreichen
					    data = "<body><p>Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean massa. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Donec quam felis, ultricies nec, pellentesque eu, pretium quis, sem. Nulla consequat massa quis enim.</p><p>Donec pede justo, fringilla vel, aliquet nec, vulputate eget, arcu. In enim justo, rhoncus ut, imperdiet a, venenatis vitae, justo. Nullam dictum felis eu pede mollis pretium. Integer tincidunt. Cras dapibus. Vivamus elementum semper nisi. Aenean vulputate eleifend tellus. Aenean leo ligula, porttitor eu, consequat vitae, eleifend ac, enim.</p><p>Aliquam lorem ante, dapibus in, viverra quis, feugiat a, tellus. Phasellus viverra nulla ut metus varius laoreet. Quisque rutrum. Aenean imperdiet. Etiam ultricies nisi vel augue. Curabitur ullamcorper ultricies nisi. Nam eget dui. Etiam rhoncus. Maecenas tempus, tellus eget condimentum rhoncus, sem quam semper libero, sit amet adipiscing sem neque sed ipsum. Nam quam nunc, blandit vel, luctus pulvinar, hendrerit id, lorem.</p><p>Maecenas nec odio et ante tincidunt tempus. Donec vitae sapien ut libero venenatis faucibus. Nullam quis ante. Etiam sit amet orci eget eros faucibus tincidunt. Duis leo. Sed fringilla mauris sit amet nibh. Donec sodales sagittis magna. Sed consequat, leo eget bibendum sodales, augue velit cursus nunc, quis gravida magna mi a libero.</p><p>Fusce vulputate eleifend sapien. Vestibulum purus quam, scelerisque ut, mollis sed, nonummy id, metus. Nullam accumsan lorem in dui. Cras ultricies mi eu turpis hendrerit fringilla. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; In ac dui quis mi consectetuer lacinia. Nam pretium turpis et arcu. Duis arcu tortor, suscipit eget, imperdiet nec, imperdiet iaculis, ipsum.</p><p>Sed aliquam ultrices mauris. Integer ante arcu, accumsan a, consectetuer eget, posuere ut, mauris. Praesent adipiscing. Phasellus ullamcorper ipsum rutrum nunc. Nunc nonummy metus. Vestibulum volutpat pretium libero. Cras id dui.</p></body>"

					    dataLength = data.size()
                            reply = reply1 + data // dabei wird dataLength in reply1 eingetragen
                            break
                    }

                    Utils.writeLog("Server", "server", "sendet: ${new String(apdu)}", 11)

                    // Antwort senden
                    stack.tcpSend([connId: connId, sdu: reply])
                }
            } // while
        } // while
    }
}

//------------------------------------------------------------------------------
