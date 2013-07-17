(ns maestro.core
  (:use [overtone.live]
        [serial-port]))

;;(System/setProperty "gnu.io.rxtx.SerialPorts" "/dev/ttyACM0")

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

(defsynth monotron
  "Korg Monotron from website diagram:
  http://korg.com/services/products/monotron/monotron_Block_diagram.jpg."
  [note     60            ; midi note value
   volume   0.7           ; gain of the output
   mod_pitch_not_cutoff 1 ; use 0 or 1 only to select LFO pitch or cutoff modification
   pitch    0.0           ; frequency of the VCO
   rate     4.0           ; frequency of the LFO
   int      1.0           ; intensity of the LFO
   cutoff   1000.0        ; cutoff frequency of the VCF
   peak     0.5]          ; VCF peak control (resonance)
  (let [note_freq       (midicps note)
        pitch_mod_coef  mod_pitch_not_cutoff
        cutoff_mod_coef (- 1 mod_pitch_not_cutoff)
        LFO             (* int (saw rate))
        VCO             (saw (+ note_freq pitch (* pitch_mod_coef LFO)))
        vcf_freq        (+ cutoff (* cutoff_mod_coef LFO) note_freq)
        VCF             (moog-ff VCO vcf_freq peak)
        ]
    (out 0 (* volume VCF))))

;;(def portname "/dev/cu.usbmodemfa131")
;;(def portname "/dev/ttyACM0")
(def portname "/dev/cu.usbmodemfd121")

(declare port)
(declare d)

(defn modify-dubstep [a]
  (ctl d :note (+ a 36))
  (ctl d :wobble (+ (Math/floor (/ a 3)) 1)))

(defn modify-monotron [a]
  (ctl d :pitch a))

(defn start! [portname]
  ;;(def d (monotron 40 0.8 1 0.0 2.5 350.0 800.0 3.0))
  (def d (dubstep))
  (def port (open portname 9600))
  (on-byte port modify-dubstep))

(defn stop! []
  (stop)
  (close port))
