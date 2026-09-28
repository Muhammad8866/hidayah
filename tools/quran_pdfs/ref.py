"""Reference layout: 11-line Mushaf, printed pages from its own index (pdf page = printed + 8)."""
import numpy as np
S11 = [2, 3, 96, 151, 209, 253, 298, 351, 371, 412, 439, 468, 494, 507, 520, 532, 561, 584, 607, 621, 641, 659, 681, 697, 716,
       730, 750, 767, 789, 806, 819, 827, 832, 853, 866, 878, 889, 903, 914, 931, 950, 963, 976, 989, 994, 1001, 1010, 1018, 1027, 1032,
       1038, 1044, 1048, 1054, 1059, 1065, 1071, 1081, 1088, 1094, 1100, 1103, 1105, 1108, 1112, 1116, 1121, 1126, 1130, 1135,
       1138, 1142, 1146, 1149, 1153, 1156, 1159, 1163, 1166, 1169, 1171, 1173,
       1174, 1177, 1179, 1181, 1182, 1183, 1185, 1187, 1188, 1189, 1191, 1192, 1192, 1193, 1194, 1195, 1196, 1197, 1198, 1199,
       1199, 1200, 1200, 1201, 1201, 1202, 1202, 1203, 1203, 1204, 1204, 1205]
J11 = [2] + [41 + 40 * i for i in range(28)] + [1163]
END11 = 1206
assert len(S11) == 114 and len(J11) == 30 and J11[28] == 1121, (len(S11), J11[28])


def pos11(page):
    """(para index, fraction) of a printed 11-line page."""
    j = max(i for i in range(30) if J11[i] <= page)
    nxt = J11[j + 1] if j < 29 else END11
    return j, (page - J11[j]) / (nxt - J11[j])


def expected_surahs(paras, end):
    """Expected pdf page of each surah in a layout whose para starts are known."""
    out = []
    for p in S11:
        j, f = pos11(p)
        nxt = paras[j + 1] if j < 29 else end
        out.append(paras[j] + f * (nxt - paras[j]))
    return out


def expected_paras(surahs, end):
    """Expected pdf page of each para in a layout whose surah starts are known."""
    out = []
    for jp in J11:
        s = max(i for i in range(114) if S11[i] <= jp and (i == 113 or S11[i + 1] > S11[i] or True))
        s = max(i for i in range(114) if S11[i] <= jp)
        nxt11 = S11[s + 1] if s < 113 and S11[s + 1] > S11[s] else END11
        f = (jp - S11[s]) / max(1, nxt11 - S11[s])
        nxtX = surahs[s + 1] if s < 113 else end
        out.append(surahs[s] + f * (nxtX - surahs[s]))
    return out
