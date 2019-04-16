from Crypto import Random
from Crypto.Cipher import AES
import os, sys,random
import base64, re
import hashlib

DEFAULT_BLOCK_SIZE = 128
RSA_BYTE_SIZE = 256
DEFAULT_ENCODEING_FORMAT='utf-8'
DEFAULT_CIPHER_KEY_SEP='@@@'

class Encryptor:
    def __init__(self, key, keyToStore):
        self.key = key
        self.keyToStore = keyToStore
        self.bys = 32   

    @staticmethod
    def dounpad(message_str):
        return message_str[:-ord(message_str[len(message_str)-1:])]
    
    def dopad(self, message_str):
        return message_str + (self.bys - len(message_str) % self.bys) * chr(self.bys - len(message_str) % self.bys)    

    def encrypt(self, raw_message):
        raw_message = self.dopad(raw_message)
        self.key = hashlib.md5(str(self.key).encode(DEFAULT_ENCODEING_FORMAT)).digest()
        print('hashlib of key', self.key)
        cipher = AES.new(self.key, AES.MODE_ECB)
        encrypted = cipher.encrypt(raw_message)

        encoded = base64.b64encode(encrypted)
        return str(encoded, DEFAULT_ENCODEING_FORMAT)   

    #use base64 to convert to string
    # Writing encrypted text @@ then encrypted_aes_key
    def encrypt_file(self, file_cip, file_txt, encrypted_aes_key):
        with open(file_txt, 'r') as fo:
            plaintext = fo.read()
        enc = self.encrypt(plaintext)
        #print('writing contentn', enc)
        #print('encrypted_aes_key key ', encrypted_aes_key, file_cip) 
        with open(file_cip, 'w') as fo:
            fo.write(enc+ DEFAULT_CIPHER_KEY_SEP +str(encrypted_aes_key))       

    @staticmethod    
    def decrypt(enc, key):
        # encode key and get MD5 hash 
        key = hashlib.md5(key.encode(DEFAULT_ENCODEING_FORMAT)).digest()
        # Decode the encrypted message Base64 
        # MODE_ECB AES Cipher 
        cipher = AES.new(key, AES.MODE_ECB)
        decrypted = cipher.decrypt(base64.b64decode(enc))
        #print('decrypted', decrypted)
        return str(Encryptor.dounpad(decrypted), DEFAULT_ENCODEING_FORMAT)   

    @staticmethod    
    def decrypt_file(file_cip, file_txt, file_one):
        # Reading rsa keys 
        with open(file_one, 'r') as fo:
            rsa_keys = fo.read()
        #print('rsa_keys for decrypt_file', rsa_keys);
        
        # Reading cipher text from message.cip to decypt
        with open(file_cip, 'r') as fo:
            ciphertext = fo.read()
        # Split the cipher text and store encrypted message
        ciphertext, key = ciphertext.split(DEFAULT_CIPHER_KEY_SEP);
        #print('ciphertext here including ency AES key ', key)    
        key = read_message_cipher_file_and_decrypt(key, file_one)

        #print('key returned after RSA decrypt', key, len(key));
        plaintext = Encryptor.decrypt(ciphertext, key)

        print('plaintext', plaintext)
        # Writing the decrypted message to the file
        with open(file_txt, 'w') as fo:
            fo.write(plaintext)


#convert list of int blocks to default_block_size string characters 
def get_blocks_from_text(message, default_block_size=DEFAULT_BLOCK_SIZE):
    message_bytes = str(message).encode(DEFAULT_ENCODEING_FORMAT)
    block_ints = []
    for block_start in range(0, len(message_bytes), default_block_size):
        block_int = 0
        for i in range(block_start, min(block_start + default_block_size, len(message_bytes))):
            block_int += message_bytes[i] * (RSA_BYTE_SIZE ** (i % default_block_size))
        block_ints.append(block_int)
    return block_ints

# divide and decode the big message block in 128 block size
def get_text_from_blocks(block_ints, message_length, default_block_size=DEFAULT_BLOCK_SIZE):
    message = []
    for block_int in block_ints:
        block_message = []
        for i in range(default_block_size - 1, -1, -1):
            if len(message) + i < message_length:
                ascii_number = block_int // (RSA_BYTE_SIZE ** i)
                block_int = block_int % (RSA_BYTE_SIZE ** i)
                block_message.insert(0, chr(ascii_number))
        message.extend(block_message)
    return ''.join(message)

# encrypt aes key by RSA block by  block using key n, e pair 
# cipher_text = pow(plaintext, e) mod n
def encrypt_message_block_by_block(message, key, default_block_size=DEFAULT_BLOCK_SIZE):
    encrypted_blocks = []
    n, e = key
    for block in get_blocks_from_text(message, default_block_size):
        encrypted_blocks.append(pow(block, e, n))
    return encrypted_blocks

# RSA decrypts all 128 sized  blocks to original message which is our AES key 
# Of Specifies length divide in 128 sized blocks 
# plain_text = pow(ciphertext, d) mod n
def decrypt_message_block_by_block(encrypted_blocks, message_length, key, default_block_size=DEFAULT_BLOCK_SIZE):
    decrypted_blocks = []
    n, d = key
    for block in encrypted_blocks:
        decrypted_blocks.append(pow(block, d, n))
    return get_text_from_blocks(decrypted_blocks, message_length, default_block_size)


def read_alice_bob_key_file(key_file_name):
    # Given the filename of a file that contains a public or private key,
    # return the key as a (n,e) or (n,d) tuple value.
    fo = open(key_file_name)
    file_content = fo.read()
    #Close file 
    fo.close()
    keySize, n, E_or_D = file_content.split(',')
    return (int(keySize), int(n), int(E_or_D))

# encypt aes key using RSA 
# Store the encypted key in the format messageLength_blockSize_encyptedAESKey

def encrypt_message_txt_file_and_Write_to_cipher_file(key_file_name, message, default_block_size=DEFAULT_BLOCK_SIZE):
    # Using a key from a key file, encrypt the message and save it to a
    # file. Returns the encrypted message string.
    key_size, n, e = read_alice_bob_key_file(key_file_name)

    # Encrypt the message
    encrypted_blocks = encrypt_message_block_by_block(message, (n, e), default_block_size)
   
    for i in range(len(encrypted_blocks)):
        encrypted_blocks[i] = str(encrypted_blocks[i])
    encrypted_content = ','.join(encrypted_blocks)
    encrypted_content = '%s_%s_%s' % (len(str(message)), default_block_size, encrypted_content)
    return encrypted_content

# split the stores key which was stores in the format messageLength_blockSize_encyptedAESKey
# Call RSA decryption algo

def read_message_cipher_file_and_decrypt(content, key_file_name):
    key_size, n, d = read_alice_bob_key_file(key_file_name)
    #print('keySize, n, d', keySize, n, d)
    #print('content', content)

    message_length, block_size, encrypted_message = content.split('_')
    message_length = int(message_length)
    block_size = int(block_size)

    # Convert the encrypted message into large int values.
    encrypted_blocks = []
    for block in encrypted_message.split(','):
        encrypted_blocks.append(int(block))

    # Decrypt the large int values.
    return decrypt_message_block_by_block(encrypted_blocks, message_length, (n, d), block_size)           


# *************************************** Execution Starting Point ******************************
print('****************************** AES Encryption and RSA key encryption ********************')
clear = lambda: os.system('cls')
print('First arg is ', sys.argv[1])
isEncy = False

if(sys.argv[1] == '-e'):
    isEncy = True

file_one = sys.argv[2]
file_two = sys.argv[3]
file_three = sys.argv[4]

#/crypt.py -e bob.pub message.txt message.cip
if(isEncy):
    keyK = random.getrandbits(DEFAULT_BLOCK_SIZE)
    keyK1 = keyK
    #print('keyK1 $$$$$$$$$$$$$$$$$$$$$$$$$ keyK1', keyK1)
    #init key to Encryptor class 
    enc = Encryptor(keyK1, keyK)

    encrypted_aes_key = encrypt_message_txt_file_and_Write_to_cipher_file(file_one, keyK)
    enc.encrypt_file(file_three, file_two, encrypted_aes_key)
    print('Encryption of message.txt file is done. cipher text is present in message.cip file ')    
#./crypt.py -d bob.prv message.cip message.txt        
else :
    Encryptor.decrypt_file(file_two, file_three, file_one)
    print('Decryption of message.cip file is done. plain text is in message.txt file ')        

print("Process is done ")