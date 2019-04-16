import random
import math
import sys
DEFAULT_KEY_SIZE = 2049
LOW_PRIMES = [2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97, 101, 103, 107, 109, 113, 127, 131, 137, 139, 149, 151, 157, 163, 167, 173, 179, 181, 191, 193, 197, 199, 211, 223, 227, 229, 233, 239, 241, 251, 257, 263, 269, 271, 277, 281, 283, 293, 307, 311, 313, 317, 331, 337, 347, 349, 353, 359, 367, 373, 379, 383, 389, 397, 401, 409, 419, 421, 431, 433, 439, 443, 449, 457, 461, 463, 467, 479, 487, 491, 499, 503, 509, 521, 523, 541, 547, 557, 563, 569, 571, 577, 587, 593, 599, 601, 607, 613, 617, 619, 631, 641, 643, 647, 653, 659, 661, 673, 677, 683, 691, 701, 709, 719, 727, 733, 739, 743, 751, 757, 761, 769, 773, 787, 797, 809, 811, 821, 823, 827, 829, 839, 853, 857, 859, 863, 877, 881, 883, 887, 907, 911, 919, 929, 937, 941, 947, 953, 967, 971, 977, 983, 991, 997]

# calcualte greates common divisor of two number
def gcd(a1, b1):
    while a1 != 0:
        a1, b1 = b1 % a1, a1
    return b1

# fine modular inversor of a and m

def find_modular_inverse(inv_a, inv_m):
    if gcd(inv_a, inv_m) != 1:
        return None 
    num_u1, num_u2, num_u3 = 1, 0, inv_a
    num_v1, num_v2, num_v3 = 0, 1, inv_m
    while num_v3 != 0:
        num_q = num_u3 // num_v3 
        num_v1, num_v2, num_v3, num_u1, num_u2, num_u3 = (num_u1 - num_q * num_v1), (num_u2 - num_q * num_v2), (num_u3 - num_q * num_v3), num_v1, num_v2, num_v3
    return num_u1 % inv_m
    
def get_primes(start_index, stop_index):
    if start_index >= stop_index:
        return []
    primes_array = [2]
    for number in range(3, stop_index + 1, 2):
        for prime in primes_array:
            if number % prime == 0:
                break
        else:
            primes_array.append(number)

    while primes_array and primes_array[0] < start_index:
        del primes_array[0]

    return primes_array


def rabin_miller_find_prime(num):
    s_num = num - 1
    t_num = 0
    while s_num % 2 == 0:
        s_num = s_num // 2
        t_num += 1

    for trials in range(5):
        a_num = random.randrange(2, num - 1)
        v_num = pow(a_num, s_num, num)
        if v_num != 1:
            i_index = 0
            while v_num != (num - 1):
                if i_index == t_num - 1:
                    return False
                else:
                    i_index = i_index + 1
                    v_num = (v_num ** 2) % num
    return True


def test_if_prime(num):
    if (num < 2):
        return False

    if num in LOW_PRIMES:
        return True

    for prime in LOW_PRIMES:
        if (num % prime == 0):
            return False
    return rabin_miller_find_prime(num)


# Return a random prime number of keysize bits in size.
def generate_large_prime_of_keySize(key_size=DEFAULT_KEY_SIZE):
    while True:
        num = random.randrange(2**(key_size-1), 2**(key_size))
        if test_if_prime(num):
            return num

    # Creates a public/private key pair with keys that are keySize bits in
    # size. This function may take a while to run.
    # Step 1: Create two prime numbers, p and q. Calculate n = p * q.
    # Step 2: Create a number e that is relatively prime to (p-1)*(q-1).
    # Keep trying random numbers for e until one is valid.
    # Step 3: Calculate d, the mod inverse of e.

def generate_private_public_key(default_key_size):

    print('********* generating p prime...........')
    p = generate_large_prime_of_keySize(default_key_size)
    print('********* generating q prime..................')
    q = generate_large_prime_of_keySize(default_key_size)
    n = p * q


    print('\n ******** generating e for (e, N) which is relatively prime to phi(n) = (p-1)*(q-1)........')
    while True:
        e = random.randrange(2 ** (default_key_size - 1), 2 ** (default_key_size))
        if gcd(e, (p - 1) * (q - 1)) == 1:
             break

    print('\n ******** calculating d for private key that is mod inverse of e... ...')
    d = find_modular_inverse(e, (p - 1) * (q - 1))
    rsa_public_key = (n, e)
    rsa_private_key = (n, d)
    return (rsa_public_key, rsa_private_key)
    

if __name__ == '__main__':
    public, private = generate_private_public_key(DEFAULT_KEY_SIZE)
    #print ("Your public key is ", public ," and your private key is ", private)
    print('\nStoring......... ', sys.argv[1] + ".pub")
    with open(sys.argv[1] + ".pub", 'w') as fo:
            fo.write('%s,%s,%s' % (DEFAULT_KEY_SIZE, public[0], public[1]))
    
    print('\nStoring.......... ', sys.argv[1] + ".prv")        
    with open(sys.argv[1] + ".prv", 'w') as fo:
            fo.write('%s,%s,%s' % (DEFAULT_KEY_SIZE, private[0], private[1]))


