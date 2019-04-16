![screenshot ](https://github.com/mukeshkdangi/encypt_aes/blob/master/Screenshot%202019-04-04%20at%201.01.05%20AM.png)
# encypt_aes
![AES Encryption ](https://github.com/mukeshkdangi/encypt_aes/blob/master/ezgif.com-video-to-gif.gif)
> Implement a system that uses AES to encrypt data and RSA to protect AES keys. You will
need to write one program to generate RSA keys, and a second one that uses the RSA keys to perform the
encryption and decryption of messages.

### Step 1 
- generate Bob/Alice key pair(private and public key) alice.pub and alice.prv
```
python genkeys.py alice
```
### Step 2
- Encrypt message.txt file using AES and also encrypt AES key using RSA 2049 
- Store the cipher text in message.cip
```
python AESTest.py -e alice.pub message.txt message.cip
```

### Step 3
- Decrypt message.cip file using alice.prv to  message.txt file 
```
python AESTest.py -d alice.prv message.cip message.txt
 ```

### Example 
```
Uses: 
mukesh@localhost in ~/Downloads/CSCI531_HW_3 on master [!?$]$ python genkeys.py bob
********* generating p prime...........
********* generating q prime..................
Storing.........  bob.pub
Storing..........  bob.prv

mukesh@localhost in ~/Downloads/CSCI531_HW_3 on master [!?$]$ python genkeys.py alice
********* generating p prime...........
********* generating q prime..................
Storing.........  alice.pub
Storing..........  alice.prv

mukesh@localhost in ~/Downloads/CSCI531_HW_3 on master [!?$]$ cat message.txt 
This is Mukesh This is Mukesh This is Mukesh This is Mukesh This is Mukesh This is Mukesh This is Mukesh This is Mukesh This is Mukesh This is Mukesh This is Mukesh This is Mukesh This is Mukesh This is Mukesh This is Mukesh This is Mukesh This is Mukesh This is Mukesh This is Mukesh This is Mukesh 
mukesh@localhost in ~/Downloads/CSCI531_HW_3 on master [!?$]$ python crypt.py -e bob.pub message.txt message.cip
Encryption of message.txt file is done. cipher text is present in message.cip file 
Process is done 

mukesh@localhost in ~/Downloads/CSCI531_HW_3 on master [!?$]$ python crypt.py -d bob.prv message.cip message.txt
plaintext This is Mukesh This is Mukesh This is Mukesh This is Mukesh This is Mukesh This is Mukesh This is Mukesh This is Mukesh This is Mukesh This is Mukesh This is Mukesh This is Mukesh This is Mukesh This is Mukesh This is Mukesh This is Mukesh This is Mukesh This is Mukesh This is Mukesh This is Mukesh 
Decryption of message.cip file is done. plain text is in message.txt file 
Process is done 

mukesh@localhost in ~/Downloads/CSCI531_HW_3 on master [!?$]$ cat message.txt
This is Mukesh This is Mukesh This is Mukesh This is Mukesh This is Mukesh This is Mukesh This is Mukesh This is Mukesh This is Mukesh This is Mukesh This is Mukesh This is Mukesh This is Mukesh This is Mukesh This is Mukesh This is Mukesh This is Mukesh This is Mukesh This is Mukesh This is Mukesh 

Using Alice Key with message2.txt
mukesh@localhost in ~/Downloads/CSCI531_HW_3 on master [!?$]$ cat message2.txt 
I have strong grasp of technology as well as business skills. Since in my previous role i had interacted with business leads and the country manager of our Asia pacific business as well as their technology teams.

mukesh@localhost in ~/Downloads/CSCI531_HW_3 on master [!?$]$ python crypt.py -e alice.pub message2.txt message2.cip
Encryption of message.txt file is done. cipher text is present in message.cip file 
Process is done 

mukesh@localhost in ~/Downloads/CSCI531_HW_3 on master [!?$]$ python crypt.py -d alice.prv message2.cip message2.txt
plaintext I have strong grasp of technology as well as business skills. Since in my previous role i had interacted with business leads and the country manager of our Asia pacific business as well as their technology teams.
Decryption of message2.cip file is done. plain text is in message.txt file 
Process is done 

mukesh@localhost in ~/Downloads/CSCI531_HW_3 on master [!?$]$ cat message2.txt 
I have strong grasp of technology as well as business skills. Since in my previous role i had interacted with business leads and the country manager of our Asia pacific business as well as their technology teams.

```
## Project Details
To complete the project, you will need to write two Python programs:
-[x] 1. genkeys.py — generate RSA public and private keys.
 The program takes a single command line argument: the name of the user for whom the keys will
be generated. For test purposes, use the user names alice and bob.

 The program must be runnable directly from the command shell, e.g., ./genkeys.py alice

 The program must generate an RSA public/private key pair using your own code (you cannot
import RSA code from another module such as PyCrypto). It must use random.SystemRandom or
os.urandom() as the source of pseudo-random bytes. The keys must be of practical cryptographic
size.

 The program must produce two output files, one containing the RSA private key (e.g., alice.prv)
and the other one containing the RSA public key (e.g., alice.pub). The format of the key files is
up to you.

 You will need to write code to compute modular inverse and to test whether a number is prime.
2. crypt.py — encrypt and decrypt data using AES-128 and RSA.

 The program takes four command line arguments: a single flag (-e or -d) indicating whether the
program will encrypt or decrypt a message, the name of the public or private key file to use
(generated by keygen.py), the name of the file to encrypt or decrypt, and the name of the output
file. For example, the following command will encrypt the file secret.txt using Alice’s public key
file alice.pub to produce the cipher text file secret.cip:

```./crypt.py -e alice.pub secret.txt secret.cip```
Then the following command would decrypt the file secret.cip:
```./crypt.py -d alice.prv secret.cip secret.txt```

Some random number examples (p,q):  
104087 104089 104107 104113 104119 104123 104147 104149 104161 104173 
104179 104183 104207 104231 104233 104239 104243 104281 104287 104297 
104309 104311 104323 104327 104347 104369 104381 104383 104393 104399 
104417 104459 104471 104473 104479 104491 104513 104527 104537 104543 
104549 104551 104561 104579 104593 104597 104623 104639 104651 104659 
104677 104681 104683 104693 104701 104707 104711 104717 104723 104729
 
 To encrypt a file, the program must generate a random key K for AES-128 using
random.SystemRandom or os.urandom(), use the key K with AES-128 to encrypt the data from
the input file, use RSA with the public key file specified on the command line to encrypt K (we
refer to the encrypted K as K’ in the following), and write the encrypted data and K’ to the output
file. The format of the output file (how to store K’ along with the encrypted data) is your choice.

 To decrypt a file, the program must read the encrypted data and K’ from the input file, RSAdecrypt K’ to recover the key K, use K with AES-128 to decrypt the data, and write the decrypted
data to the output file.

 You must write the RSA code; you may not import RSA code from another module such as
PyCrypto.

 You must choose an appropriate mode of operation for AES-128.
In addition to the two Python programs, you must provide a written description of the design of your
programs and a screen capture of a session demonstrating that your programs work. For example, a screen
capture of the following sequence of commands would be sufficient (this assumes the files message.txt
and message2.txt already exist):
```python
./genkeys.py alice
./genkeys.py bob
cat message.txt
./crypt.py -e bob.pub message.txt message.cip
cat message.cip
./crypt.py -d bob.prv message.cip message.txt
cat message.txt
cat message2.txt
./crypt -e alice.pub message2.txt message2.cip
cat message2.cip
./crypt -d alice.prv message2.cip message2.txt
cat message2.txt
```


