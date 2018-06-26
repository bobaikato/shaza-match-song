# README #

### Song Signature Matches ###

Please implement a function to return the N highest scored song signatures that are considered similar to another song signature.

* Each song signature has a score and a list of song signatures it is most similar to.
* The full list of similar song candidates for a given song signature is its entire similarity network (similarities, similarities of similarities, etc.). Note that the similarity relationship is bidirectional. (when A is similar to B, it's implicit that B is similar to A)
* The order of the song matches does not matter.
* The original song signature shouldn't be considered in the result.

You may assume the number of requested song matches for a song signature will never be negative, but it may be zero.

**Example:** Given A, B, C and D songs with the following scores and similarities given as input to our program:
![song-similarities](https://gitlab.uk.shazamteam.net/amp/song-matches/raw/master/song-similarities.jpg)

```
     $> song A 1.1
     $> song B 3.3
     $> song C 2.5
     $> song D 4.7
     $> similar A B
     $> similar A C
     $> similar B D
     $> similar C D
```


* getSongMatches(A, 2): should return {B, D}

```
    $> getSongMatches A 2
    $> result B D
```


* getSongMatches(A, 4) should return {B, C, D}

```
    $> getSongMatches A 4
    $> result B C D
```


* getSongMatches(A, 1) should return D

```
    $> getSongMatches A 1
    $> result D
```

Please design a solution which operates as a library.
