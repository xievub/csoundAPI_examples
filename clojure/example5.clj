; Example 5 - Generating Score
; Author: Steven Yi <stevenyi@gmail.com>
; 2013.10.28
;
; In this example, we will look at three techniques for generating our Score. 
; 
; The first is one we have already seen, which is to just write out the score
; by hand as a String.
;
; Knowing that we pass strings into Csound to pass note events, we can also
; generate the string.  In the second example, sco2 is generated by mapping 
; over a sequence from 0 to 12 (range 0 13), formatting each value with a 
; string. The value is used twice, once for the start time, and again for the
; Csound PCH value. The result is an ascending note line. 
;
; In the final example, we are going to generate a list of lists.  The top-level
; list represents our score as a whole, and each sub-list within it represents
; the data for a single note.  The main list is then processed in two ways: first,
; it processes each sub-list and joins the values together into a single note string;
; second, it joins each individual note string into a single, large score string,
; separated by newlines.  The end result is a sequence of 13 notes with random
; pitches.
;
; The final example represents a common pattern of development.  For systems that
; employ some event-based model of music, it is common to use some kind of data
; structure to represent events.  This may use some kind of common data structure
; like a list, or it may be represented by using a class and instances of that
; class. 
;
; Note, the three examples here are indicated with comments.  To listen to the examples,
; look for the lines that have (ReadScore. c sco) (lines 86-88), uncomment the one
; you want to hear, and comment out the others. 

(import [csnd6 csnd6 Csound] 
        [java.util Random])
(require '[clojure.string :refer [join]])

; this line turns off Csound's atexit handler as well as signal handlers
(csnd6/csoundInitialize (bit-or csnd6/CSOUNDINIT_NO_ATEXIT csnd6/CSOUNDINIT_NO_SIGNAL_HANDLER))

; Defining our Csound ORC code within a triple-quoted, multline String
(def orc "
sr=44100
ksmps=32
nchnls=2
0dbfs=1

instr 1 
ipch = cps2pch(p5, 12)
kenv linsegr 0, .05, 1, .05, .7, .4, 0
aout vco2 p4 * kenv, ipch 
aout moogladder aout, 2000, 0.25
outs aout, aout
endin")


; Example 1 - Static Score 
(def sco "i1 0 1 0.5 8.00")

;# Example 2 - Generating Score string using a map with range sequence
(def sco2
  (join "\n" (map #(format "i1 %g .25 0.5 8.%02d" (* % 0.25) %) 
                  (range 0 13))))


; Example 3 - Generating Score using intermediate data structure (list of lists),
;             then converting to String.

; initialize a list to hold lists of values representing notes
(def notes 
  (let [r (Random.)]
    (map #(vector 1 (* % 0.25) 0.25 0.5 (format "8.%02d" (.nextInt r 15)))
         (range 0 13))))

; convert list into a single string
(def sco3
  (join "\n" (map #(str "i" (join " " %)) notes)))



(let [c (Csound.)]
  (.SetOption c "-odac") ; Using SetOption() to configure Csound
  ; Note: use only one commandline flag at a time
  (.CompileOrc c orc)    ; Compile the Csound Orchestra String
  ;(.ReadScore c sco)     ; Read in Score from pre-written String 
  ;(.ReadScore c sco2)     ; Read in Score from sequence generated String 
  (.ReadScore c sco3)     ; Read in Score from sequence generated String 
  (.Start c)             ; When compiling from strings, this call is necessary before doing any performing 

  ; The following is our main performance loop. We will perform one block of sound at a time 
  ; and continue to do so while it returns 0, which signifies to keep processing.  We will
  ; explore this loop technique in further examples. 
  (loop [retval 0]
    (when (zero? retval)
      (recur (.PerformKsmps c))))
  (.Stop c))

