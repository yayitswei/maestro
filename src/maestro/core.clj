(ns maestro.core
  (:use [overtone.live]
        [serial-port]))

(System/setProperty "gnu.io.rxtx.SerialPorts" "/dev/ttyACM0")

(defsynth dubstep [bpm 120 wobble 1 note 50 snare-vol 1 kick-vol 1 v 1]
 (let [trig (impulse:kr (/ bpm 120))
       freq (midicps note)
       swr (demand trig 0 (dseq [wobble] INF))
       sweep (lin-exp (lf-tri swr) -1 1 40 3000)
       wob (apply + (saw (* freq [0.99 1.01])))
       wob (lpf wob sweep)
       wob (* 0.8 (normalizer wob))
       wob (+ wob (bpf wob 1500 2))
       wob (+ wob (* 0.2 (g-verb wob 9 0.7 0.7)))

       kickenv (decay (t2a (demand (impulse:kr (/ bpm 30)) 0 (dseq [1 0 0 0 0 0 1 0 1 0 0 1 0 0 0 0] INF))) 0.7)
       kick (* (* kickenv 7) (sin-osc (+ 40 (* kickenv kickenv kickenv 200))))
       kick (clip2 kick 1)

       snare (* 3 (pink-noise [1 1]) (apply + (* (decay (impulse (/ bpm 240) 0.5) [0.4 2]) [1 0.05])))
       snare (+ snare (bpf (* 4 snare) 2000))
       snare (clip2 snare 1)]

   (out 0 (* v (clip2 (+ wob (* kick-vol kick) (* snare-vol snare)) 1)))))



                                        ; (def portname "/dev/cu.usbmodemfa131")
(def portname "/dev/ttyACM0")


(declare port)
(declare d)

(defn start!
  []
  (def d (dubstep))
  (def port (open portname 9600))
  (on-byte port #(do
                   (ctl d :note (+ % 36))
                   (ctl d :wobble (+ (Math/floor (/ % 3)) 1)))))

(defn stop!
  []
  (stop)
  (close port)
)
