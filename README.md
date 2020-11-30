# README #

### Song Matches ###

All songs are given a score, to represent the quality of their metadata.
We would like to return results to the end user with as high quality metadata as possible.

Songs are also marked as similar to other songs.
This can be represented by what we call a _similarity graph_.

Given a number _**n**_ and a _similarity graph_ of songs, return the _**n**_ highest scoring similar songs for a given song.

* Each given song has a score and a list of similar songs.
* Similarity is commutative i.e. if A is similar to B then B is similar to A.
* Similarity is transitive i.e. if A is similar to B, and B is similar to C, then A is similar to C.
* The order of the songs that are returned does not matter.
* The original song should not be considered in the result.
* If _**n**_ is more than the number of similar songs, then return all of the similar songs.
* If _**n**_ is zero, return no similar songs.

**Example:** Given songs A, B, C and D with the following scores and similarities are given as input to your program:

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

Please write a solution that is production ready.
