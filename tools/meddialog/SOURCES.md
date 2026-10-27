# Sources for the clinical follow-up questions

The follow-up questions in `lib/deep.mjs` are **hand-written** from standard history-taking guidance. They were not mined from MedDialog and not copied from any page: each question is original wording, and the scoring (which answers point toward or away from which condition) is a judgement call, written down in the open so a clinician can review it.

The pages below were found by web search while building the question banks (October 2026). Most are teaching summaries, family-medicine review articles or reference sites, not formal guidelines. Where a search returned only thin or patient-education material, that is noted; those banks lean more on general clinical knowledge and need the most review.

| Complaint | What informed the questions | Quality of what was found |
|---|---|---|
| Headache | [UCSF Hospital Handbook: Approach to Headache](https://hospitalhandbook.ucsf.edu/node/1711), [Ophthalmology Times](https://ophthalmologytimes.com/view/asking-right-questions-leads-better-headache-diagnosis); ICHD-3 features and the SNOOP red-flag list as summarised there | Reasonable (clinical summaries) |
| Chest pain | [University of Michigan open course notes](https://open.umich.edu/sites/default/files/downloads/082312.keagle.approachtothepatientwithchestpain.pdf) | Teaching notes; notes that no single history feature separates the causes |
| Stomach pain | [AAFP 2008: acute abdominal pain](https://www.aafp.org/pubs/afp/issues/2008/0401/p971.html) | Good |
| Cough | [UCSF Hospital Handbook: Approach to Cough](https://hospitalhandbook.ucsf.edu/node/1696), [BMJ Best Practice differentials](https://bestpractice.bmj.com/topics/en-us/69/differentials) | Reasonable |
| Shortness of breath | [AAFP 2005: dyspnea](https://www.aafp.org/pubs/afp/issues/2005/0415/p1529.pdf), [PMC5247680](https://pmc.ncbi.nlm.nih.gov/articles/PMC5247680/) | Good |
| Dizziness | [AAFP 2010: Dizziness, a diagnostic approach](https://www.aafp.org/pubs/afp/issues/2010/0815/p361.html) | Good |
| Back or neck pain | [MDedge: nocturnal back pain](https://mdedge.com/rheumatology/article/101959/pain/nocturnal-back-pain-suggests-systemic-disorder), [survey: mechanical or inflammatory low back pain](https://researchportal.murdoch.edu.au/esploro/outputs/journalArticle/Mechanical-or-inflammatory-low-back-pain/991005543919707891) | Mixed; mostly secondary sources |
| Urinary problem | [AAFP 2015: Dysuria in adults](https://www.aafp.org/afp/2015/1101/p778.html) | Good |
| Period or pregnancy | [AAFP 2013: Amenorrhea](https://www.aafp.org/afp/2013/0601/p781.html), [AAFP 2019](https://www.aafp.org/afp/2019/0701/p39.html) | Good |
| Palpitations | [AAFP 2005: Palpitations](https://www.aafp.org/pubs/afp/issues/2005/0215/p743.html), [Merck Manual](https://www.merckmanuals.com/en-ca/professional/cardiovascular-disorders/symptoms-of-cardiovascular-disorders/palpitations) | Good |
| Fever | [AAFP: fever in the returned traveller](https://www.aafp.org/afp/2003/1001/p1343), [PubMed 35469592](https://pubmed.ncbi.nlm.nih.gov/35469592/) | Travel-focused; non-travel adult fever and UTI features came from general knowledge |
| Rash | [AAFP 2010: The generalized rash, diagnostic approach](https://www.aafp.org/afp/2010/0315/p735) | Good |
| Sore throat, cold, sinus | [WikEM: pharyngitis](https://wikem.org/wiki/Pharyngitis), [Centor/McIsaac calculator](https://www.evigrade.com/en/calculators/centor-mcisaac) | Centor criteria well covered; allergic rhinitis and sinusitis features came from general knowledge |
| Vomiting or diarrhea | [AAFP 2014: Acute diarrhea in adults](https://www.aafp.org/afp/2014/0201/p180), [StatPearls](https://www.ncbi.nlm.nih.gov/books/NBK513295/) | Good for infection; medicine, pregnancy and migraine causes came from general knowledge |
| Joint pain | [Harvard Health: OA vs RA](https://content.health.harvard.edu/blog/what-is-the-difference-between-osteoarthritis-and-rheumatoid-arthritis), [MIMS](https://www.mims.com/india/disease/osteoarthritis/differential-diagnosis) | Reasonable for OA/RA/gout; injury features from general knowledge |
| Numbness or tingling | [Spine-health](https://www.spine-health.com/conditions/neck-pain/diagnosing-cervical-radiculopathy-vs-other-causes-hand-pain) | **Weak** (patient-education sites); needs the most review |
| Tiredness | [UCSF Hospital Handbook: Approach to Fatigue](https://hospitalhandbook.ucsf.edu/node/1681) | Reasonable |
| Anxiety | [GAD-7 (University of Washington)](https://www.hiv.uw.edu/page/mental-health-screening/gad-7); DSM-5 criteria from general knowledge | Search gave screening-tool detail, little on telling GAD from panic apart |
| Low mood | [APA: PHQ-9](https://www.apa.org/pi/about/publications/caregivers/practice-settings/assessment/tools/patient-health); bipolar and grief screening from general knowledge | PHQ-9 well covered; **bipolar screening was not found in search** |
| Acne | [AMBOSS: acne and rosacea](https://resident360.amboss.com/adult-medicine/dermatology/acne-and-rosacea/acne-and-rosacea.html) | **Thin**; folliculitis and when-to-see-a-doctor came from general knowledge |

## Safety notes

- Any answer that matters for safety is handled by hand-written rules, not by scoring alone: the emergency screen comes first, serious conditions (kidney infection, appendicitis, kidney stone, heart failure, typhoid, malaria or dengue, pneumonia, angina, vestibular neuritis) are always screened before the questions may stop, and a few answers (progressive weakness, fainting, a completely irregular pulse, unrelenting pain with weight loss) raise urgency directly.
- Point values were set by judgement, not fitted to data. `node verify_deep.mjs` tests that the question bank is wired correctly (a textbook patient for each condition reaches that condition) and that urgency is never lower than the condition warrants. It does **not** measure clinical accuracy.
- All advice text in `lib/kb.mjs` and `lib/kb_deep.mjs` is general information and must be reviewed by a clinician before any public release.
