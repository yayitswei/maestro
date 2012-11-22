(ns maestro.core)

(def portname "/dev/cu.usbmodemfa131")
(def port (open portname 9600))
(on-byte port #(println %))
