from Crypto.Cipher import AES
from Crypto import Random
import os
import sys
import os.path
from os import listdir
from os.path import isfile, join
import ast
import time
import string
import math
import random
import base64

AES_128 = 128
import base64
import hashlib
from Crypto import Random
from Crypto.Cipher import AES

class AESCipher(object):

    def __init__(self, key): 
        self.bs = 32
        self.key = hashlib.sha256(key.encode()).digest()

    def encrypt(self, raw):
        raw = self._pad(raw)
        iv = Random.new().read(AES.block_size)
        cipher = AES.new(self.key, AES.MODE_CBC, iv)
        return base64.b64encode(iv + cipher.encrypt(raw))

    def decrypt(self, enc):
        enc = base64.b64decode(enc)
        iv = enc[:AES.block_size]
        cipher = AES.new(self.key, AES.MODE_CBC, iv)
        return self._unpad(cipher.decrypt(enc[AES.block_size:])).decode('utf-8')

    def _pad(self, s):
        return s + (self.bs - len(s) % self.bs) * chr(self.bs - len(s) % self.bs)

    @staticmethod
    def _unpad(s):
        return s[:-ord(s[len(s)-1:])]

class EncryptorRSA:
    def __init__(self, key):
        print('AES.block_size', key)
        self.key = key

    def pad(self, s):
        return s + "0" * (AES.block_size - len(s) % AES.block_size)

    def encrypt(self, message, key, key_size=128):
        message = self.pad(message)
        iv = Random.new().read(AES.block_size)
        cipher = AES.new(key, AES.MODE_CBC, iv)
        return iv + cipher.encrypt(message)

    def encrypt_file(self, file_name, file_three, encrypted_aes_key):
        with open(file_name, 'r') as fo:
            plaintext = fo.read()
        #print('plaintext................', plaintext);  
        #print((self.key))  
        enc = self.encrypt(plaintext,  self.key)
        print('writing cipher text ', enc);
        with open(file_three, 'w') as fo:
            fo.write(str(base64.b64encode(enc)))
        with open(file_three, 'a') as fo:
            fo.write(str("@@@")) 
        with open(file_three, 'a') as fo:
            fo.write(str(base64.b64encode(self.key)))    

        print('pringting file content .................................')    
        with open(file_three, 'r') as fo:
            print(fo.read())    


    def decrypt(self, ciphertext, key):
        iv = ciphertext[:AES.block_size]
        cipher = AES.new(key, AES.MODE_CBC, iv)
        plaintext = cipher.decrypt(ciphertext[AES.block_size:])
        return plaintext.rstrip("0")

    def decrypt_file(self, cipher, file_three, decrypted_aes_key):
        print('cipher @@@@@@@@@@@@ ', cipher);
        print('decrypted_aes_key @@@@@@@@@@@@ ', decrypted_aes_key);
        dec = self.decrypt(cipher, decrypted_aes_key)
        with open(file_three, 'w') as fo:
            fo.write(dec)
           
    def encryptRSA(self, file_text):
        publicKey = [int(x) for x in file_text.split("#")]
        key = publicKey[0]
        n = publicKey[1]
        plaintext = self.key
        #Convert each letter in the plaintext to numbers based on the character using a^b mod m
        lists = [str(x) for x in str(plaintext)]
        cipher=[pow(ord(char), key, n)  for char in lists]

        #Return the array of bytes
        return cipher

  
    def decryptRSA(self, pk, ciphertext):
        #Unpack the key into its components
        key, n = pk
        #Generate the plaintext based on the ciphertext and key using a^b mod m
        
        ciphertext=ciphertext[1:-2]
        print('ciphertext for decryptRSA',ciphertext)
        lists = [int(x) for x in ciphertext.split("#")]
        print('lists', lists)
        plain = [pow(chr(char), key, n) for char in lists]
        #Return the array of bytes as a string
        #print('plain', plain)
        return ''.join(plain)



clear = lambda: os.system('cls')
isEncy = False;

print('First arg is ', sys.argv[1])

if(sys.argv[1] == '-e'):
    isEncy = True

file_one = sys.argv[2]
file_two = sys.argv[3]
file_three = sys.argv[4]
#/crypt.py -e bob.pub message.txt message.cip
if(isEncy):
    keyK = os.urandom(32)
    #keyK = random.getrandbits(256)
    #print('keyK', len(str(keyK)))
    enc = Encryptor(keyK)
    with open(file_one, 'r') as fo:
        text = fo.read()
        encrypted_aes_key = enc.encryptRSA(text)
        enc.encrypt_file(file_two, file_three, text)
#./crypt.py -d bob.prv message.cip message.txt        
else :
    with open(file_one, 'r') as fo:
        text = fo.read()
        pirvateKey = [int(x) for x in text.split("#")]
        with open(file_two, 'r') as fos:
            s = fos.read()
            print('ciphertext', base64.b64decode(s + b'=' * (-len(s) % 4)))
            cipher, enc_key = base64.b64decode(s + b'=' * (-len(s) % 4)).split("@@@")
            #print('pirvateKey', pirvateKey)
            print('enc_key #################', enc_key)
            #print('enc_key', enc_key)
            enc = Encryptor("")
            #decrypted_aes_key = enc.decryptRSA(pirvateKey, enc_key)
            enc.decrypt_file(cipher, file_three, enc_key)

print("Process is done ")