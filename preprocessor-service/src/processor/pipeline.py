import preprocessor as p
from bs4 import BeautifulSoup
import html
import re
import contractions
import unicodedata

WS_RE = re.compile(r"\s+")

p.set_options(p.OPT.URL, p.OPT.EMOJI,p.OPT.HASHTAG,p.OPT.RESERVED,p.OPT.MENTION,p.OPT.SMILEY,p.OPT.ESCAPE_CHAR)

def clean_tweet(t):
    return p.clean(t)

def strip_html(s: str) -> str:
    soup = BeautifulSoup(s or "", "html.parser")
    for t in soup(["script", "style"]):
        t.decompose()
    text = soup.get_text(separator=" ", strip=True)
    return html.unescape(text)

def clean_quotes(text):
    return re.sub(r"['\"]", "", str(text))

def remove_elongated_tokens(text, threshold=2):
    tokens = text.split()
    clean_tokens = []
    for tok in tokens:
        if re.search(r"(.)\1{%d,}" % (threshold + 1), tok):
            continue
        clean_tokens.append(tok)
    return " ".join(clean_tokens)

ABBR_SET = {
    "u","ur","btw","idk","imo","imho",
    "omg","omfg",
    "lol","lmao","rofl",
    "brb","bbl","bfn",
    "b4","bc",
    "thx","tx","ty","tysm",
    "np","nvm",
    "wtf","wth",
    "idc","ikr",
    "tbh","tbf",
    "asap","afaik",
    "afk",
    "bff","bffl",
    "bf","gf",
    "ily","ilu","ilysm",
    "jk","jkjk",
    "gg",
    "g2g","gtg",
    "ttyl","ttys",
    "smh",
    "fyi",
    "ftw",
    "irl",
    "tmi",
    "yw",
    "omw",
}

def remove_abbrev(text):
    tokens = text.split()
    tokens = [t for t in tokens if t.lower() not in ABBR_SET]
    return " ".join(tokens)

def keep_short_tweets(text):
    if text is None:
        return text

    words = str(text).split()
    if len(words) >= 40:
        return None
    return text

def preprocess_pipeline(text):
    if text is None:
        return None
    text = clean_tweet(text)
    text = strip_html(text)
    text = clean_quotes(text)
    text = remove_elongated_tokens(text)
    text = remove_abbrev(text)
    text = contractions.fix(text)
    text = keep_short_tweets(text)
    return text

def lemma_string(client, text: str) -> str:
    t = unicodedata.normalize("NFKC", str(text) if text is not None else "")
    t = WS_RE.sub(" ", t).strip()

    if not t:
        return ""

    try:
        ann = client.annotate(t)
        lemmas = [tok.lemma for sent in ann.sentence for tok in sent.token]
        return " ".join(lemmas)
    except Exception as e:
        print(f"Lỗi khi lemmatize: {e}")
        return t
