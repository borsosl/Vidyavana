
export const errorCode: {[key: string]: string} = {
    EML: 'Helytelen e-mail formátum',
    PASS_DIFFER: 'A jelszók nem egyeznek',
    PASS_EMPTY: 'Hiányzik a jelszó',
    PASS_SHORT: 'A jelszó minimum 5 karakter'
};


/**
 * @return valid email or throws Error with errorCode key
 */
export function email(email: string): string {
    email = email.trim();
    if(!/\S+@\S+\.\S+/.test(email)) {
        throw new Error('EML');
    }
    return email;
}


/**
 * @return valid password or throws Error with errorCode key
 */
export function password(pwd1: string, pwd2?: string, emptyOK: boolean = false): string {
    if(pwd1)
        pwd1 = pwd1.trim();
    if(pwd2)
        pwd2 = pwd2.trim();
    if(pwd2 !== undefined && pwd1 !== pwd2)
        throw new Error('PASS_DIFFER');
    if(!pwd1.length) {
        if(!emptyOK)
            throw new Error('PASS_EMPTY');
    } else if(pwd1.length < 5)
        throw new Error('PASS_SHORT');
    return pwd1;
}
