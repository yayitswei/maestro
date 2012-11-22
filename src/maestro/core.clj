(ns maestro.core)

(def portname "/dev/cu.usbmodemfa131")
(def port (open portname 9600))
(on-n-bytes port 4 #(println %))
